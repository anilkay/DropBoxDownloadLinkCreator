import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetTemporaryLinkResult;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Scanner;

public class Main extends Application {
    String FileLink = null;
    DbxClientV2 client;
    Boolean hasToken = true;

    public Main() throws IOException {
        String ACCESS_TOKEN = "";
        String desktop = System.getProperty("user.home") + "/Desktop";
        FileReader reader = new FileReader(desktop+"/jarlar/t9ken.txt");
        Scanner scanner = new Scanner(reader);
        if (scanner.hasNext()) {
            ACCESS_TOKEN = scanner.next();
            System.out.println(ACCESS_TOKEN);
        } else {
            hasToken = false;
        }
        DbxRequestConfig config = DbxRequestConfig.newBuilder("anilkay").build();
        client = new DbxClientV2(config, ACCESS_TOKEN);
    }

    public static void main(String[] args) {
        String desktop = System.getProperty("user.home") + "/Desktop";
        System.out.println(desktop);
        launch(args);
    }

    public void start(final Stage primaryStage) throws Exception {
        if (hasToken == false) {
            getTokenError();
        }
        primaryStage.setTitle("Dropbox Link Creator");

        Button button = new Button();

        button.setText("Dosya Seçin");

        BorderPane layout = new BorderPane();

        final Label label = new Label("Link hazır değil");

        HBox hBox = new HBox();
        hBox.setPadding(new Insets(15, 12, 15, 12));
        hBox.setSpacing(10);

        Button copyButton = new Button("Copy to Clipboard");
        hBox.getChildren().addAll(label, copyButton);

        final Clipboard clipboard = Clipboard.getSystemClipboard();
        copyButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                ClipboardContent clipboardContent = new ClipboardContent();
                if (FileLink != null && !FileLink.isEmpty()) {
                    clipboardContent.putString(FileLink);
                    clipboard.setContent(clipboardContent);
                }
            }
        });

        layout.setTop(hBox);
        layout.setBottom(button);

        final Alert alert = new Alert(Alert.AlertType.INFORMATION);

        Scene scene = new Scene(layout, 300, 300);

        final FileChooser chooser = new FileChooser();
        final File[] file = new File[1];
        button.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                file[0] = chooser.showOpenDialog(primaryStage);
                File file1 = file[0];
                System.out.println(file1.getName());
                System.out.println(file1.getAbsolutePath());

                try {
                    String uploadandsharelink = uploadandsharelink(file1.getAbsolutePath());
                    FileLink = uploadandsharelink;
                    alert.setTitle("Link");
                    alert.setContentText(uploadandsharelink);
                    alert.show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DbxException e) {
                    e.printStackTrace();
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public String uploadandsharelink(String filepath) throws FileNotFoundException, DbxException {

        InputStream in = new FileInputStream(filepath);
        File file = new File(filepath);
        String fileName = file.getName();
        String uploadPath = "/upload/" + fileName; //Başa da istiyormuş bir şapka
        try {
            FileMetadata fileMetadata = client.files().uploadBuilder(uploadPath).uploadAndFinish(in);
            String all = fileMetadata.toStringMultiline();
            System.out.println(all);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GetTemporaryLinkResult result = client.files().getTemporaryLink(uploadPath);
        String link = result.getLink();
        System.out.println(link);
        return link; //I think it is worked.

    }

    public void getTokenError() {
        Alert alert = new Alert(Alert.AlertType.ERROR, "Content here", ButtonType.OK);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.show();
    }
}
