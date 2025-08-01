package Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.*;

public class SupplierAgent extends Agent {
    private Map<String, SupplierInfo> suppliers;
    
    protected void setup() {
        System.out.println("🏢 SupplierAgent " + getAID().getName() + " démarré");
        
        initializeSuppliers();
        
        // Ajouter les comportements
        addBehaviour(new RestockRequestBehaviour());
        addBehaviour(new SupplierEvaluationBehaviour());
        
        System.out.println("✅ SupplierAgent prêt - Gestion des fournisseurs active");
    }
    
    private void initializeSuppliers() {
        suppliers = new HashMap<>();
        // l'initialisation des fournisseurs à partir de la base de données (table: fournisseurs) n'a pas encore faite.
        suppliers.put("TechDistrib", new SupplierInfo("TechDistrib", 4.5, 2, 0.95));
        suppliers.put("ComputerWorld", new SupplierInfo("ComputerWorld", 4.2, 3, 0.90));
        suppliers.put("AccessoirePro", new SupplierInfo("AccessoiresInformatiques", 4.8, 1, 0.98));
    }
    
    private class RestockRequestBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                
                if (content.startsWith("RESTOCK_REQUEST")) {
                    handleRestockRequest(msg);
                }
            } else {
                block();
            }
        }
        
        private void handleRestockRequest(ACLMessage msg) {
            String[] parts = msg.getContent().split("\\|");
            if (parts.length >= 4) {
                int productId = Integer.parseInt(parts[1]);
                String productName = parts[2];
                int quantity = Integer.parseInt(parts[3]);
                
                System.out.println("📋 Demande de réapprovisionnement: " + productName + " x" + quantity);
                
                // Sélectionner le meilleur fournisseur
                SupplierInfo bestSupplier = selectBestSupplier(productName);
                
                if (bestSupplier != null) {
                    // Simuler la commande fournisseur
                    int deliveryDays = bestSupplier.deliveryTime + new Random().nextInt(2);
                    Date expectedDelivery = new Date(System.currentTimeMillis() + (deliveryDays * 24 * 60 * 60 * 1000));
                    
                    // Créer une commande fournisseur
                    String orderRef = "SUP" + System.currentTimeMillis();
                    
                    // Notifier le StockAgent de la commande passée
                    ACLMessage stockMsg = new ACLMessage(ACLMessage.INFORM);
                    stockMsg.addReceiver(new jade.core.AID("stock", jade.core.AID.ISLOCALNAME));
                    stockMsg.setContent("SUPPLIER_ORDER_PLACED|" + productId + "|" + quantity + "|" + 
                                      bestSupplier.name + "|" + orderRef + "|" + expectedDelivery);
                    stockMsg.setConversationId("supplier-orders");
                    send(stockMsg);
                    
                    // Notifier le ManagerAgent
                    ACLMessage managerMsg = new ACLMessage(ACLMessage.INFORM);
                    managerMsg.addReceiver(new jade.core.AID("manager", jade.core.AID.ISLOCALNAME));
                    managerMsg.setContent("SUPPLIER_ORDER|Commande " + orderRef + " passée chez " + 
                                        bestSupplier.name + " pour " + productName + " x" + quantity);
                    managerMsg.setConversationId("supplier-updates");
                    send(managerMsg);
                    
                    // Programmer la livraison (simulation)
                    addBehaviour(new SimulateSupplierDeliveryBehaviour(productId, quantity, deliveryDays * 1000));
                    
                    System.out.println("📦 Commande fournisseur créée: " + orderRef + " chez " + bestSupplier.name);
                } else {
                    System.out.println("❌ Aucun fournisseur disponible pour " + productName);
                }
            }
        }
        
        private SupplierInfo selectBestSupplier(String productName) {
            // Logique de sélection du meilleur fournisseur
            // Critères: fiabilité, délai de livraison, score qualité
            SupplierInfo best = null;
            double bestScore = 0;
            
            for (SupplierInfo supplier : suppliers.values()) {
                // Score composite: (fiabilité * 0.4) + (score qualité * 0.4) + (bonus temps de livraison * 0.2)
                double deliveryBonus = Math.max(0, (7 - supplier.deliveryTime) / 7.0); // Bonus pour livraison rapide
                double score = (supplier.reliability * 0.4) + (supplier.qualityScore * 0.4) + (deliveryBonus * 0.2);
                
                if (score > bestScore) {
                    bestScore = score;
                    best = supplier;
                }
            }
            
            return best;
        }
    }
    
    private class SimulateSupplierDeliveryBehaviour extends WakerBehaviour {
        private int productId;
        private int quantity;
        
        public SimulateSupplierDeliveryBehaviour(int productId, int quantity, long delay) {
            super(SupplierAgent.this, delay);
            this.productId = productId;
            this.quantity = quantity;
        }
        
        protected void onWake() {
            // Simuler la réception de la livraison fournisseur
            ACLMessage stockMsg = new ACLMessage(ACLMessage.REQUEST);
            stockMsg.addReceiver(new jade.core.AID("stock", jade.core.AID.ISLOCALNAME));
            
            // Obtenir le stock actuel et ajouter la quantité livrée
            stockMsg.setContent("UPDATE_STOCK|" + productId + "|+" + quantity); // +quantity pour addition
            stockMsg.setConversationId("supplier-delivery");
            send(stockMsg);
            
            // Notifier le ManagerAgent
            ACLMessage managerMsg = new ACLMessage(ACLMessage.INFORM);
            managerMsg.addReceiver(new jade.core.AID("manager", jade.core.AID.ISLOCALNAME));
            managerMsg.setContent("SUPPLIER_DELIVERY|Réception livraison fournisseur: +" + quantity + 
                                " unités pour produit " + productId);
            managerMsg.setConversationId("supplier-updates");
            send(managerMsg);
            
            System.out.println("📦 Livraison fournisseur reçue: +" + quantity + " unités (Produit " + productId + ")");
        }
    }
    
    // Simulation d'évaluation des fournisseurs ( Cette tâche peut être confiée à un système d'IA)
    private class SupplierEvaluationBehaviour extends TickerBehaviour {
        public SupplierEvaluationBehaviour() {
            super(SupplierAgent.this, 600000); // Évaluation toutes les 5 minutes
        }
        
        protected void onTick() {
            // Simulation d'évaluation périodique des fournisseurs
            for (SupplierInfo supplier : suppliers.values()) {
                // Fluctuation aléatoire légère des scores
                Random random = new Random();
                double variation = (random.nextDouble() - 0.5) * 0.1; // ±0.05
                supplier.reliability = Math.max(1.0, Math.min(5.0, supplier.reliability + variation));
                
                System.out.println("📊 Évaluation fournisseur " + supplier.name + ": " + 
                                 String.format("%.2f", supplier.reliability) + "/5.0");
            }
        }
    }
    
    // Classe interne pour les informations fournisseur
    private class SupplierInfo {
        String name;
        double reliability; // Score de 1 à 5
        int deliveryTime; // Jours
        double qualityScore; // Score de 0 à 1
        
        public SupplierInfo(String name, double reliability, int deliveryTime, double qualityScore) {
            this.name = name;
            this.reliability = reliability;
            this.deliveryTime = deliveryTime;
            this.qualityScore = qualityScore;
        }
    }
    
    protected void takeDown() {
        System.out.println("🏢 SupplierAgent terminé");
    }
}
