package app.gui;

import app.client.InterfaceHandler;
import app.client.InterfaceRouter;
import app.gui.media.TrackPlayer;
import app.model.Boss;
import app.model.Info;
import app.model.Room;
import app.network.messages.Message;
import app.network.messages.MessageTypes;
import app.network.messages.SocketMessage;
import app.services.LoggerService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.stage.WindowEvent;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.ArrayList;

import static app.services.LoggerService.println;

public class GameGUI extends Application implements UI {
    private BossesController bosses;
    private BossController currentBoss;
    public RoomController currentRoom;
    private RoomTakeController roomTake;
    private TrackPlayer trackPlayer = new TrackPlayer("battle.mp3");;
    private RegistrationController registrationPage;
    private LoginController loginPage;
    private MenuController menuPage;

    public static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/login.fxml"));
        Parent root = loader.load();
        loginPage = loader.getController();
        loginPage.init(this);

        primaryStage.setTitle("RPG clicker");
        primaryStage.setScene(new Scene(root));

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event){

                ArrayList<String> userInfo = new ArrayList<>();
                userInfo.add(InterfaceHandler.getInstance(GameGUI.this).getSession().getUsername());
                userInfo.add(String.valueOf(InterfaceHandler.getInstance(GameGUI.this).getSession().getRoomId()));
                userInfo.add(String.valueOf(InterfaceHandler.getInstance(GameGUI.this).getSession().getClicksCount()));
                SocketMessage message = new SocketMessage(MessageTypes.DISCONNECT,userInfo);
                InterfaceHandler.getInstance(GameGUI.this).interfaceService.sendMessage(message);
                Platform.exit();
                System.exit(0);

                try {
                    stop();
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }
            }
        });

        primaryStage.show();

    }

    // routes
    public void toSignUp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/sign-up.fxml"));
            Parent root = loader.load();
            registrationPage = loader.getController();
            registrationPage.init(this);
            primaryStage.setTitle("Registration");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException ex) {
            println(LoggerService.level.ERROR.name(),"client","Error with loading sign up ui");
        }
    }

    public void toLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/login.fxml"));
            Parent root = loader.load();
            loginPage = loader.getController();
            loginPage.init(this);
            primaryStage.setTitle("Login");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException ex) {
            println(LoggerService.level.ERROR.name(),"client","Error with loading login ui");
        }
    }

    public void toMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/menu.fxml"));
            Parent root = loader.load();
            menuPage = loader.getController();
            menuPage.init(this);
            primaryStage.setTitle("Menu");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            trackPlayer.stop();
        } catch (IOException ex) {
            println(LoggerService.level.ERROR.name(),"client","Error with loading menu ui");
        }
    }

    public void toBoss(Boss boss, int roomId) {
        if (boss.access <= InterfaceHandler.getInstance(this).getSessionUserClicks()
            || boss.access <= InterfaceHandler.getInstance(this).getSession().getRoomClicksCount()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/bosses/boss.fxml"));
                Parent root = loader.load();
                currentBoss = loader.getController();
                currentBoss.init(this, boss.name, boss.health, boss.viewPath, roomId);
                primaryStage.setTitle("Boss");
                primaryStage.setScene(new Scene(root));
                primaryStage.show();
                trackPlayer.play();
            } catch (IOException ex) {
                println(LoggerService.level.ERROR.name(),"client","Error with loading boss ui");
            }
        }
    }

    public void toBoses() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/bosses.fxml"));
            Parent root = loader.load();
            bosses = loader.getController();
            bosses.init(this, null);
            primaryStage.setTitle("Bosses");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            trackPlayer.stop();
        } catch (IOException ex) {
            println(LoggerService.level.ERROR.name(),"client","Error with loading bosses ui");
        }
    }

    public void toBoses(Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/bosses.fxml"));
            Parent root = loader.load();
            bosses = loader.getController();
            bosses.init(this, room);
            primaryStage.setTitle("Bosses");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            trackPlayer.stop();
        } catch (IOException ex) {
            println(LoggerService.level.ERROR.name(),"client","Error with loading bosses ui");
        }
    }

    public void toRoomInput() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/room-take.fxml"));
            Parent root = loader.load();
            roomTake = loader.getController();
            roomTake.init(this);
            primaryStage.setTitle("Room");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException ex) {
            println(LoggerService.level.ERROR.name(),"client","Error with loading room ui");
        }
    }

    public void toRoom(int roomId, Room room) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/assets/room.fxml"));
            Parent root = loader.load();
            currentRoom = loader.getController();
            currentRoom.init(this, roomId, room);
            primaryStage.setTitle("Room");
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            trackPlayer.stop();
        } catch (IOException ex) {
            println(LoggerService.level.ERROR.name(),"client","Error with loading room ui");
        }
    }

    @Override
    public void getAnswer(Message message) {
        messageHandler(message);
    }

    public void messageHandler(Message message) {
        // handle here messages from the backend
        Info infoFormServer = InterfaceRouter.getRoute(message, InterfaceHandler.getInstance(this));
        switch(infoFormServer.getRoute()){
            case "menu":{
                toMenu();
                break;
            }
            case "signUp": {
                toSignUp();
                break;
            }
            case "login": {
                toLogin();
                break;
            }
            case "single": {
                toBoses();
                break;
            }
            case "room": {
                // брать из объекта
                toRoom((Integer) infoFormServer.getPayload().get(1), (Room) infoFormServer.getPayload().get(2));
                break;
            }
            // multiplayer
            case "bosses":
            case "leaveBossRoom": {

                toBoses((Room) infoFormServer.getPayload().get(2));
                break;
            }
            case "toBossFromRoom": {
                toBoss((Boss) infoFormServer.getPayload().get(2), (Integer) infoFormServer.getPayload().get(1));
                break;
            }
            case "updateBossData": {
                this.currentBoss.updateBossData();
                break;
            }
            default: {
                toSignUp();
            }
        }
    }
}
