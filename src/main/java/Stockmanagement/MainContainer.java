package Stockmanagement;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import db.DatabaseManager;

public class MainContainer {
    
    public static void main(String[] args) {
        try {
            // Initialiser la base de données
            DatabaseManager.getInstance().initializeDatabase();
            
            // Créer le runtime JADE
            Runtime rt = Runtime.instance();
            
            // Créer un profil par défaut
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.GUI, "true");
            
            // Créer le conteneur principal
            AgentContainer mainContainer = rt.createMainContainer(profile);
            
            // Créer et démarrer les agents
            AgentController managerAgent = mainContainer.createNewAgent(
                "manager", "Agents.ManagerAgent", null);
            
            AgentController stockAgent = mainContainer.createNewAgent(
                "stock", "Agents.StockAgent", null);
            
            AgentController orderAgent = mainContainer.createNewAgent(
                "order", "Agents.OrderAgent", null);
            
            AgentController deliveryAgent = mainContainer.createNewAgent(
                "delivery", "Agents.DeliveryAgent", null);
            
            AgentController supplierAgent = mainContainer.createNewAgent(
                "supplier", "Agents.SupplierAgent", null);
            
            // Démarrer tous les agents
            managerAgent.start();
            stockAgent.start();
            orderAgent.start();
            deliveryAgent.start();
            supplierAgent.start();
            
            System.out.println("=== Système de Gestion de Stock JADE démarré ===");
            System.out.println("Agents actifs:");
            System.out.println("- ManagerAgent");
            System.out.println("- StockAgent");
            System.out.println("- OrderAgent");
            System.out.println("- DeliveryAgent");
            System.out.println("- SupplierAgent");
            
        } catch (StaleProxyException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
