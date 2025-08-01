package Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

public class DeliveryAgent extends Agent {
    private Map<Integer, DeliveryInfo> pendingDeliveries;
    
    protected void setup() {
        System.out.println("🚚 DeliveryAgent " + getAID().getName() + " démarré");
        
        pendingDeliveries = new HashMap<>();
        
        // Ajouter les comportements
        addBehaviour(new DeliverySchedulingBehaviour());
        addBehaviour(new DeliveryTrackingBehaviour());
        
        System.out.println("✅ DeliveryAgent prêt - Gestion des livraisons active");
    }
    
    private class DeliverySchedulingBehaviour extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                
                if (content.startsWith("SCHEDULE_DELIVERY")) {
                    handleDeliveryScheduling(msg);
                }
            } else {
                block();
            }
        }
        
        private void handleDeliveryScheduling(ACLMessage msg) {
            String[] parts = msg.getContent().split("\\|");
            if (parts.length >= 4) {
                int orderId = Integer.parseInt(parts[1]);
                String address = parts[2];
                String customerName = parts[3];
                
                // Créer une info de livraison
                DeliveryInfo delivery = new DeliveryInfo(orderId, address, customerName);
                pendingDeliveries.put(orderId, delivery);
                
                // Simulation de la planification
                delivery.estimatedDelivery = new Date(System.currentTimeMillis() + (2 * 60 * 1000)); // +2 minutes
    
                delivery.carrier = selectCarrier(address);
                delivery.trackingNumber = generateTrackingNumber();
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = sdf.format(delivery.estimatedDelivery);
                
                // Confirmer la planification
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("DELIVERY_SCHEDULED|" + orderId + "|" + delivery.trackingNumber + "|" + formattedDate);
                send(reply);
                
                // Notifier le ManagerAgent
                ACLMessage managerMsg = new ACLMessage(ACLMessage.INFORM);
                managerMsg.addReceiver(new jade.core.AID("manager", jade.core.AID.ISLOCALNAME));
                managerMsg.setContent("DELIVERY_UPDATE|Livraison planifiée pour commande :" + orderId + 
                                    "|Transporteur:" + delivery.carrier + "|Suivi:" + delivery.trackingNumber + 
                                    "|Date éstimée de livraison :" + formattedDate);
                managerMsg.setConversationId("delivery-updates");
                send(managerMsg);
                
                System.out.println("📅 Livraison planifiée: Commande " + orderId + " via " + delivery.carrier);
            }
        }
        
        private String selectCarrier(String address) {
            // Logique aléatoire de sélection de transporteur
        	// Un modèle d'IA peut remplacé cette logique de choix de livreur
            String[] carriers = {"Poste", "DHL", "Aramex", "Atlas Livraison"};
            return carriers[new Random().nextInt(carriers.length)];
        }
        
        private String generateTrackingNumber() {
            return "TRK" + System.currentTimeMillis() + new Random().nextInt(1000);
        }
    }
    
    private class DeliveryTrackingBehaviour extends TickerBehaviour {
        public DeliveryTrackingBehaviour() {
            super(DeliveryAgent.this, 120000); // Vérification toutes les 2 minutes
        }
        
        protected void onTick() {
            for (DeliveryInfo delivery : pendingDeliveries.values()) {
                if (delivery.status.equals("SCHEDULED")) {
                    // Simuler l'évolution du statut
                    Random random = new Random();
                    if (random.nextBoolean()) {
                        delivery.status = "IN_TRANSIT";
                        
                        // Notifier le changement de statut
                        ACLMessage managerMsg = new ACLMessage(ACLMessage.INFORM);
                        managerMsg.addReceiver(new jade.core.AID("manager", jade.core.AID.ISLOCALNAME));
                        managerMsg.setContent("DELIVERY_UPDATE|Commande " + delivery.orderId + 
                                            " : en transit |Suivi:" + delivery.trackingNumber);
                        managerMsg.setConversationId("delivery-updates");
                        send(managerMsg);
                        
                        System.out.println("🚛 Livraison en transit: " + delivery.trackingNumber);
                    }
                } else if (delivery.status.equals("IN_TRANSIT")) {
                    // Possibilité de livraison
                    Random random = new Random();
                    if (random.nextInt(5) == 0) { // 20% de chance
                        delivery.status = "DELIVERED";
                        delivery.actualDelivery = new Date();
                        
                        ACLMessage managerMsg = new ACLMessage(ACLMessage.INFORM);
                        managerMsg.addReceiver(new jade.core.AID("manager", jade.core.AID.ISLOCALNAME));
                        managerMsg.setContent("DELIVERY_UPDATE|Commande " + delivery.orderId + 
                                            " :livrée à" + delivery.customerName);
                        managerMsg.setConversationId("delivery-updates");
                        send(managerMsg);
                        
                        System.out.println("✅ Livraison terminée: " + delivery.trackingNumber);
                    }
                }
            }
        }
    }
    
    // Classe interne pour les informations de livraison
    private class DeliveryInfo {
        int orderId;
        String address;
        String customerName;
        String status = "SCHEDULED";
        String carrier;
        String trackingNumber;
        Date estimatedDelivery;
        Date actualDelivery;
        
        public DeliveryInfo(int orderId, String address, String customerName) {
            this.orderId = orderId;
            this.address = address;
            this.customerName = customerName;
        }
    }
    
    protected void takeDown() {
        System.out.println("🚚 DeliveryAgent terminé");
    }
}
