package Agents;

import jade.core.Agent;

import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import db.DatabaseManager;
import gui.ManagerGUI;
import gui.StockTableWindow;

public class ManagerAgent extends Agent {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ManagerGUI gui;
	private DatabaseManager dbManager;
	
	
    protected void setup() {
        System.out.println("👨‍💼 ManagerAgent " + getAID().getName() + " démarré");
        
        // Enregistrer le service dans le Directory Facilitator
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("stock-management");
        sd.setName("manager-service");
        dfd.addServices(sd);
        
        //Instanciation de l'objet dbManager
        dbManager = DatabaseManager.getInstance();
        
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        
        // Créer et afficher l'interface graphique    
        SwingUtilities.invokeLater(() -> {
            gui = new gui.ManagerGUI(this); // précise le package si nécessaire
            
            //ImageIcon icon = new ImageIcon(getClass().getResource("C:\\Users\\OUABOU\\.eclipse\\sma\\src\\main\\ressources\\icon.png"));
            //gui.setIconImage(icon.getImage());
            gui.setVisible(true);
            System.out.println("✅ ManagerAgent prêt - Interface graphique lancée");
        });
        
        // Ajouter les comportements
        addBehaviour(new SystemCoordinatorBehaviour());
        addBehaviour(new RequestProcessorBehaviour());
        
        System.out.println("✅ ManagerAgent prêt - Interface graphique lancée");
    }
    
    protected void takeDown() {
        if (gui != null) {
            gui.dispose();  
        }
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("👨‍💼 ManagerAgent terminé");
    }
    
    // Comportement de coordination système
    private class SystemCoordinatorBehaviour extends TickerBehaviour {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public SystemCoordinatorBehaviour() {
            super(ManagerAgent.this, 30000); // Vérification toutes les 30 secondes
        }
        
        protected void onTick() {
            // Envoyer des demandes de statut aux autres agents
            sendStatusRequest("stock");
            sendStatusRequest("order");
            sendStatusRequest("delivery");
            sendStatusRequest("supplier");
        }
        
        private void sendStatusRequest(String agentName) {
            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(getLocalAID(agentName));
            msg.setContent("STATUS_REQUEST");
            msg.setConversationId("system-monitoring");
            send(msg);
        }
    }
    
    // Comportement de traitement des requêtes
    private class RequestProcessorBehaviour extends CyclicBehaviour {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void action() {
            //MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            //ACLMessage msg = receive(mt);
			ACLMessage msg = receive(); // sans filtre
            
            if (msg != null) {
                String content = msg.getContent();

                if (content.startsWith("CREATE_ORDER")) {
                    handleOrderCreation(msg);
                } else if (content.startsWith("STOCK_ALERT")) {
                    handleStockAlert(msg);
                } else if (content.startsWith("DELIVERY_UPDATE")) {
                	if (content.contains("Livraison planifiée pour commande"))
						try {
							handleDeliveryCreation(msg);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					else if (content.contains("en transit") || content.contains("livrée"))
                		handleDeliveryUpdate(msg);
                	
                } else if (content.startsWith("STOCK_STATUS|")) {
                    SwingUtilities.invokeLater(() -> {
                        new gui.StockTableWindow(content); // Ouvre la fenêtre dans l'EDT
                    });
                } 
                
            } else {               
                block();
            }
        }
        
        private void handleOrderCreation(ACLMessage msg) {
            // Transférer la demande à OrderAgent
            ACLMessage orderMsg = new ACLMessage(ACLMessage.REQUEST);
            orderMsg.addReceiver(getLocalAID("order"));
            orderMsg.setContent(msg.getContent());
            orderMsg.setConversationId("order-processing");
            send(orderMsg);
            
            if (gui != null) {
                gui.addLogMessage("📝 Nouvelle commande transmise à OrderAgent");
            }
        }
        
        
        private void handleDeliveryCreation(ACLMessage msg) throws ParseException {
        	String content = msg.getContent();
        	// System.out.println("Contenu reçu dans handleDeliveryCreation : " + content);
        	// extraction des données à inserer dans la base de données-table livraisons
    	    String[] parts = content.split("\\|");
    	    String orderPart = parts[1]; 
    	    String carrierPart = parts[2]; 
    	    String trackingPart = parts[3];
    	    String estimatedDeliveryPart = parts[4];

    	    // Extraire les données à inserer dans la base de données-table livraisons
    	    int orderId = Integer.parseInt(orderPart.split(":")[1].trim());
    	    
    	    // récupération du status le la livraison
    	    String statusStr = orderPart.split(":")[0].trim();
    	    String[] statusStrList = statusStr.split(" ");   
    	    String status = statusStrList[1];
    	    
    	    String carrier = carrierPart.split(":")[1].trim();
    	    String trackingNumber = trackingPart.split(":")[1].trim();
    	    
    	    // récupération et formatage de la date éstimée de la livraison
    	    String dateStr = estimatedDeliveryPart.split("Date éstimée de livraison :")[1].trim();
    	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	    java.util.Date estimatedDelivery = sdf.parse(dateStr);
    	    
    	 // Conversion java.util.Date -> java.sql.Date
            java.sql.Date sqlEstimatedDelivery = new java.sql.Date(estimatedDelivery.getTime());


    	    // Appel à la base de données pour l'insertion ou la mise à jour de la table "livraisons"
    	    try {
				dbManager.handleDeliveryCreate(orderId, status, carrier, trackingNumber, sqlEstimatedDelivery);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            if (gui != null) {
                gui.addLogMessage("🚚 Nouvelle informations à propos de la livraison de la commande n°" + orderId);
            }
        }
        
        
        private void handleStockAlert(ACLMessage msg) {
            if (gui != null) {
                gui.addLogMessage("⚠️ Alerte stock: " + msg.getContent());
            }
        }
        
        private void handleDeliveryUpdate(ACLMessage msg) {
        	String content = msg.getContent();
        	// System.out.println("Contenu reçu dans handleDeliveryCreation : " + content);
        	// extraction des données à inserer dans la base de données-table livraisons
    	    String[] parts = content.split("\\|");
    	    String orderPart = parts[1]; 
			String status = orderPart.split(":")[1].trim();
			int orderId = Integer.parseInt(orderPart.split(" ")[1].trim());

			dbManager.handleDeliveryUpdate(orderId, status);
			
            if (gui != null) {
                gui.addLogMessage("🚚 Mise à jour livraison: " + content);
            }
        }
    }
    
    // Méthode utilitaire pour obtenir l'AID d'un agent
    private jade.core.AID getLocalAID(String localName) {
        return new jade.core.AID(localName, jade.core.AID.ISLOCALNAME);
    } 
    
    
    // Méthodes publiques pour l'interface graphique
    public void sendOrderRequest(String customerName, String productName, int quantity, String address) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(getLocalAID("order"));
        msg.setContent(String.format("CREATE_ORDER|%s|%s|%d|%s", customerName, productName, quantity, address));
        msg.setConversationId("order-from-gui");
        send(msg);
        
        gui.addLogMessage("📝 Commande envoyée: " + productName + " x" + quantity);
    }
    
    public void requestStockStatus() {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(getLocalAID("stock"));
        msg.setContent("GET_STOCK_STATUS");
        msg.setConversationId("stock-inquiry");
        send(msg);
        
        gui.addLogMessage("📊 Demande de statut stock envoyée");
    }
}
