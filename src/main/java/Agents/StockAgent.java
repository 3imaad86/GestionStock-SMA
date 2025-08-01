package Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import db.DatabaseManager;
import java.util.List;

public class StockAgent extends Agent {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DatabaseManager dbManager;
    
    protected void setup() {
        System.out.println("📦 StockAgent " + getAID().getName() + " démarré");
        
        dbManager = DatabaseManager.getInstance();
        
        // Ajouter les comportements
        addBehaviour(new StockMonitoringBehaviour());
        addBehaviour(new StockRequestHandlerBehaviour());
        
        System.out.println("✅ StockAgent prêt - Surveillance des stocks active");
    }
    
    // Comportement de surveillance des stocks
    private class StockMonitoringBehaviour extends TickerBehaviour {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public StockMonitoringBehaviour() {
            super(StockAgent.this, 60000); // Vérification toutes les minutes
        }
        
        protected void onTick() {
            List<DatabaseManager.Product> products = dbManager.getAllproduits();
            for (DatabaseManager.Product product : products) {
                if (product.quantite <= product.min_quantite) {
                    sendLowStockAlert(product);
                }
            }
        }
        
        private void sendLowStockAlert(DatabaseManager.Product product) {
            // Alerter le ManagerAgent
            ACLMessage alertMsg = new ACLMessage(ACLMessage.INFORM);
            alertMsg.addReceiver(new jade.core.AID("manager", jade.core.AID.ISLOCALNAME));
            alertMsg.setContent("STOCK_ALERT|" + product.nom + "|" + product.quantite + "|" + product.min_quantite);
            alertMsg.setConversationId("stock-monitoring");
            send(alertMsg);
            
            // Demander réapprovisionnement au SupplierAgent
            ACLMessage supplierMsg = new ACLMessage(ACLMessage.REQUEST);
            supplierMsg.addReceiver(new jade.core.AID("supplier", jade.core.AID.ISLOCALNAME));
            supplierMsg.setContent("RESTOCK_REQUEST|" + product.id + "|" + product.nom + "|" + (product.min_quantite * 3));
            supplierMsg.setConversationId("restock-request");
            send(supplierMsg);
            
            System.out.println("⚠️ Stock faible détecté: " + product.nom + " (" + product.quantite + " restants)");
        }
    }
    
    // Comportement de traitement des demandes de stock
    private class StockRequestHandlerBehaviour extends CyclicBehaviour {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void action() {
            MessageTemplate mt = MessageTemplate.or(
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.QUERY_REF)
            );
            ACLMessage msg = receive(mt);
            
            if (msg != null) {
                String content = msg.getContent();
                
                if (content.equals("GET_STOCK_STATUS")) {
                    handleStockStatusRequest(msg);
                } else if (content.startsWith("CHECK_AVAILABILITY")) {
                    handleAvailabilityCheck(msg);
                } else if (content.startsWith("RESERVE_STOCK")) {
                    handleStockReservation(msg);
                } else if (content.startsWith("UPDATE_STOCK")) {
                    handleStockUpdate(msg);
                }
            } else {
                block();
            }
        }
        
        private void handleStockStatusRequest(ACLMessage msg) {
            List<DatabaseManager.Product> products = dbManager.getAllproduits();
            StringBuilder status = new StringBuilder("STOCK_STATUS|");
            
            for (DatabaseManager.Product product : products) {
                status.append(product.toString()).append(";");
            }
            
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(status.toString());
            send(reply);
            
            System.out.println("📊 Statut stock envoyé à " + msg.getSender().getLocalName());
        }
        
        private void handleAvailabilityCheck(ACLMessage msg) {
            String[] parts = msg.getContent().split("\\|");
            if (parts.length >= 3) {
                String productName = parts[1];
                int requestedQuantity = Integer.parseInt(parts[2]);
                
                List<DatabaseManager.Product> products = dbManager.getAllproduits();
                DatabaseManager.Product targetProduct = null;
                
                for (DatabaseManager.Product product : products) {
                    if (product.nom.equals(productName)) {
                        targetProduct = product;
                        break;
                    }
                }
                
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                
                if (targetProduct != null && targetProduct.quantite >= requestedQuantity) {
                    reply.setContent("AVAILABLE|" + productName + "|" + targetProduct.quantite);
                } else {
                    reply.setContent("NOT_AVAILABLE|" + productName + "|" + 
                                   (targetProduct != null ? targetProduct.quantite : 0));
                }
                
                send(reply);
                System.out.println("🔍 Vérification disponibilité: " + productName + " x" + requestedQuantity);
            }
        }
        
        private void handleStockReservation(ACLMessage msg) {
            String[] parts = msg.getContent().split("\\|");
            if (parts.length >= 3) {
                int productId = Integer.parseInt(parts[1]);
                int quantity = Integer.parseInt(parts[2]);
                
                List<DatabaseManager.Product> products = dbManager.getAllproduits();
                DatabaseManager.Product targetProduct = null;
                
                for (DatabaseManager.Product product : products) {
                    if (product.id == productId) {
                        targetProduct = product;
                        break;
                    }
                }
                
                ACLMessage reply = msg.createReply();
                
                if (targetProduct != null && targetProduct.quantite >= quantity) {
                    int newQuantity = targetProduct.quantite - quantity;
                    if (dbManager.updateStock(productId, newQuantity)) {
                        reply.setPerformative(ACLMessage.CONFIRM);
                        reply.setContent("STOCK_RESERVED|" + productId + "|" + quantity);
                        System.out.println("✅ Stock réservé: " + targetProduct.nom + " x" + quantity);
                    } else {
                        reply.setPerformative(ACLMessage.FAILURE);
                        reply.setContent("RESERVATION_FAILED|Database error");
                    }
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("INSUFFICIENT_STOCK|" + productId);
                }
                
                send(reply);
            }
        }
        
        private void handleStockUpdate(ACLMessage msg) {
            String[] parts = msg.getContent().split("\\|");
            if (parts.length >= 3) {
                int productId = Integer.parseInt(parts[1]);
                int newQuantity = Integer.parseInt(parts[2]);
                
                if (dbManager.updateStock(productId, newQuantity)) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.CONFIRM);
                    reply.setContent("STOCK_UPDATED|" + productId + "|" + newQuantity);
                    send(reply);
                    
                    System.out.println("📦 Stock mis à jour: Produit " + productId + " -> " + newQuantity + " unités");
                }
            }
        }
    }
    
    protected void takeDown() {
        System.out.println("📦 StockAgent terminé");
    }
}
