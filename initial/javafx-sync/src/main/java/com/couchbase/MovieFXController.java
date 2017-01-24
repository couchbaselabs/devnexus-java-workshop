package com.couchbase;

import com.couchbase.lite.*;
import com.couchbase.lite.Database.ChangeListener;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.util.*;

public class MovieFXController implements Initializable {

    private CouchbaseSingleton couchbase;

    @FXML
    private TextField fxTitle;

    @FXML
    private TextField fxGenre;

    @FXML
    private CheckBox fxDigital;

    @FXML
    private CheckBox fxBluray;

    @FXML
    private CheckBox fxDvd;

    @FXML
    private ListView fxListView;

    @FXML
    private Button fxSave;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        try {
            this.couchbase = CouchbaseSingleton.getInstance();
            fxListView.getItems().addAll(this.couchbase.query());
            this.couchbase.getDatabase().addChangeListener(new ChangeListener() {
                @Override
                public void changed(Database.ChangeEvent event) {
                    for(int i = 0; i < event.getChanges().size(); i++) {
                        final Document retrievedDocument = couchbase.getDatabase().getDocument(event.getChanges().get(i).getDocumentId());
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                int documentIndex = indexOfByDocumentId(retrievedDocument.getId(), fxListView.getItems());
                                for(int j = 0; j < fxListView.getItems().size(); j++) {
                                    if(((Movie)fxListView.getItems().get(j)).getDocumentId().equals(retrievedDocument.getId())) {
                                        documentIndex = j;
                                        break;
                                    }
                                }
                                if (retrievedDocument.isDeleted()) {
                                    if (documentIndex > -1) {
                                        fxListView.getItems().remove(documentIndex);
                                    }
                                } else {
                                    if (documentIndex == -1) {
                                        fxListView.getItems().add(new Movie(retrievedDocument));
                                    } else {
                                        fxListView.getItems().remove(documentIndex);
                                        fxListView.getItems().add(new Movie(retrievedDocument));                                    }
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        fxListView.setCellFactory(new Callback<ListView<Movie>, ListCell<Movie>>(){
            @Override
            public ListCell<Movie> call(ListView<Movie> p) {
                ListCell<Movie> cell = new ListCell<Movie>(){
                    @Override
                    protected void updateItem(Movie t, boolean bln) {
                        super.updateItem(t, bln);
                        String item = "";
                        ArrayList<String> formats = new ArrayList<String>();
                        if (t != null) {
                            item = t.getTitle() + ", " + t.getGenre();
                            if(t.getDigitalCopy()) {
                                formats.add("Digital");
                            }
                            if(t.getBluray()) {
                                formats.add("Blu-Ray");
                            }
                            if(t.getDvd()) {
                                formats.add("DVD");
                            }
                            item += " [" + String.join("/", formats) + "]";
                            setText(item);
                        }
                    }
                };
                return cell;
            }
        });
        fxSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if(!fxTitle.getText().equals("") && !fxGenre.getText().equals("")) {
                    fxListView.getItems().add(couchbase.save(new Movie(fxTitle.getText(), fxGenre.getText(), fxDigital.isSelected(), fxBluray.isSelected(), fxDvd.isSelected())));
                    fxTitle.setText("");
                    fxGenre.setText("");
                    fxDigital.setSelected(false);
                    fxBluray.setSelected(false);
                    fxDvd.setSelected(false);
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Missing Information");
                    alert.setHeaderText(null);
                    alert.setContentText("Both a title and genre are required for this example.");
                    alert.showAndWait();
                }
            }
        });
    }

    private int indexOfByDocumentId(String needle, ObservableList<Movie> haystack) {
        int result = -1;
        for(int i = 0; i < haystack.size(); i++) {
            if(haystack.get(i).getDocumentId().equals(needle)) {
                result = i;
                break;
            }
        }
        return result;
    }

}
