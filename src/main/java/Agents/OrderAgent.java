package Agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.sql.SQLException;

import db.DatabaseManager;

public class OrderAgent extends Agent {

	private static final long serialVersionUID = 1L;
	private DatabaseManager dbManager;

    protected void setup() {
        System.out.println("🛒 OrderAgent " + getAID().getName() + " démarré");

        dbManager = DatabaseManager.getInstance();

        addBehaviour(new OrderProcessingBehaviour());

        System.out.println("✅ OrderAgent prêt - Traitement des commandes actif");
    }

    private class OrderProcessingBehaviour extends CyclicBehaviour {
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = receive(mt);

            if (msg != null) {
                String content = msg.getContent();
                if (content.startsWith("CREATE_ORDER")) {
                    try {
                        handleOrderCreation(msg);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                block();
            }
        }

        private void handleOrderCreation(ACLMessage msg) throws SQLException {
            String[] parts = msg.getContent().split("\\|");
            if (parts.length >= 5) {
                String customerName = parts[1];
                String productName = parts[2];
                int quantity = Integer.parseInt(parts[3]);
                String address = parts[4];

                int productId = dbManager.getProductIdBynom(productName);

                System.out.println("🛒 Traitement commande: " + productName + " x" + quantity + " pour " + customerName);

                // Vérification disponibilité stock
                ACLMessage stockMsg = new ACLMessage(ACLMessage.QUERY_REF);
                stockMsg.addReceiver(new jade.core.AID("stock", jade.core.AID.ISLOCALNAME));
                stockMsg.setContent("CHECK_AVAILABILITY|" + productName + "|" + quantity);
                stockMsg.setConversationId("availability-check-" + System.currentTimeMillis());
                send(stockMsg);

                // Lancer comportement pour attendre la réponse stock
                addBehaviour(new WaitForStockResponseBehaviour(msg, customerName, productId, quantity, address, productName));
            }
        }
    }

    private class WaitForStockResponseBehaviour extends OneShotBehaviour {
        private ACLMessage originalMsg;
        private String customerName, address, productName;
        private int quantity, productId;
        private double unitPrice , total_amount;

        public WaitForStockResponseBehaviour(ACLMessage originalMsg, String customerName,
                                             int productId, int quantity, String address, String productName) {
            this.originalMsg = originalMsg;
            this.customerName = customerName;
            this.productId = productId;
            this.quantity = quantity;
            this.address = address;
            this.productName = productName;
            
        }

        public void action() {
            try {
                this.unitPrice = dbManager.getUnitPriceByName(productName);
                total_amount = quantity * unitPrice;
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }

            MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                MessageTemplate.MatchSender(new jade.core.AID("stock", jade.core.AID.ISLOCALNAME))
            );

            ACLMessage stockResponse = blockingReceive(mt, 5000);

            if (stockResponse != null && stockResponse.getContent().startsWith("AVAILABLE")) {
                processAvailableOrder();
            } else {
                processUnavailableOrder();
            }
        }

        private void processAvailableOrder() {
            int orderId = dbManager.createOrder(customerName, address, total_amount);
            int orderItemsId = dbManager.createOrderItems(orderId , productId , quantity , unitPrice);

			if (orderId > 0 &&  orderItemsId > 0) {
			    // Réserver le stock
			    ACLMessage reserveMsg = new ACLMessage(ACLMessage.REQUEST);
			    reserveMsg.addReceiver(new jade.core.AID("stock", jade.core.AID.ISLOCALNAME));
			    reserveMsg.setContent("RESERVE_STOCK|" + productId + "|" + quantity);
			    reserveMsg.setConversationId("stock-reservation");
			    send(reserveMsg);

			    // Notifier le DeliveryAgent
			    ACLMessage deliveryMsg = new ACLMessage(ACLMessage.REQUEST);
			    deliveryMsg.addReceiver(new jade.core.AID("delivery", jade.core.AID.ISLOCALNAME));
			    deliveryMsg.setContent("SCHEDULE_DELIVERY|" + orderId + "|" + address + "|" + customerName);
			    deliveryMsg.setConversationId("delivery-scheduling");
			    send(deliveryMsg);

			    // Réponse au ManagerAgent
			    ACLMessage reply = originalMsg.createReply();
			    reply.setPerformative(ACLMessage.CONFIRM);
			    reply.setContent("ORDER_CREATED|" + orderId + "|" + customerName + "|" + total_amount);
			    send(reply);

			    System.out.println("✅ Commande créée avec succès: ID " + orderId + ", Montant total = " + total_amount);
			}
        }

        private void processUnavailableOrder() {
            ACLMessage reply = originalMsg.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("ORDER_REFUSED|INSUFFICIENT_STOCK|" + productName);
            send(reply);

            System.out.println("❌ Commande refusée: Stock insuffisant pour " + productName);
        }
    }

    protected void takeDown() {
        System.out.println("🛒 OrderAgent terminé");
    }
}
