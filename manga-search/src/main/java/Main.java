import static sam.fx.helpers.FxKeyCodeUtils.combination;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sam.fx.clipboard.FxClipboard;
import sam.fx.helpers.FxFxml;
import sam.fx.helpers.FxTable;
import sam.fx.textsearch.FxTextSearch;
import sam.io.serilizers.DataReader;
import sam.io.serilizers.DataWriter;
import sam.manga.samrock.SamrockDB;
import sam.myutils.Checker;
import sam.myutils.MyUtilsException;
import sam.myutils.System2;
import sam.nopkg.SerializeHelper;
import sam.reference.WeakAndLazy;
// import sam.fx.helpers.FxConstants;

public class Main extends Application implements ChangeListener<Data> {
	public static void main(String[] args) {
		launch(args);
	}
	
	private final SerializeHelper<List<Data>> data_serialize = data_serialize();  
	private final SerializeHelper<HashMap<Integer, List<Chap>>> lastChapList_serialize = lastChapListPath();
	
	private final ByteBuffer buffer = ByteBuffer.allocate(8124);
	@FXML private TableView<Data> table;
	@FXML private TableView<Data> table2;
	@FXML private Text lastChapT;
	@FXML private TextField searchTF;
	@FXML private Button copyBtn;
	@FXML private Button reloadBtn;

	private TableViewSelectionModel<Data> model;
	private final FxTextSearch<Data> search = new FxTextSearch<>(Data::getDirnameLowercased, 300, true);

	private List<Data> data;
	Stage stage;

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		FxFxml.load(this, stage, this);
		prepareTable();
		prepareSearchTF(stage);

		data = data_serialize.read();
		if(Checker.isEmpty(data))
			reload(null);
		else
			setData();

		copyBtn.disableProperty().bind(Bindings.isEmpty(table2.getItems()));
		stage.show();
	}

	private SerializeHelper<HashMap<Integer, List<Chap>>> lastChapListPath() {
		return new SerializeHelper<HashMap<Integer, List<Chap>>>(Paths.get("lastChapList")) {
			
			@Override
			protected ByteBuffer buffer(int size) {
				return buffer;
			}
			@Override
			protected void write(DataWriter w, HashMap<Integer, List<Chap>> lastChapsList) throws IOException {
				w.writeInt(lastChapsList.size());

				for (Entry<Integer, List<Chap>> e : lastChapsList.entrySet()) {
					w.writeInt(e.getKey());
					int s = e.getValue().size();
					w.writeInt(s);
					
					if(s == 0)
						continue;

					for (Chap d : e.getValue()) {
						if(d == null)
							w.writeDouble(-100);
						else {
							w.writeDouble(d.number);
							w.writeUTF(d.name);
						}
					}
				}
			}
			
			@Override
			protected HashMap<Integer, List<Chap>> read(DataReader w) throws IOException {
				HashMap<Integer, List<Chap>> map = new HashMap<>();
				
				int size = w.readInt();
				
				for (int i = 0; i < size; i++) {
					int id = w.readInt();
					int s = w.readInt();
					List<Chap> list = new ArrayList<>(s == 0 ? 2 : s);
					map.put(id, list);
					
					if(s == 0)
						continue;
					
					for (int j = 0; j < s; j++) 
						list.add(new Chap(w.readDouble(), w.readUTF()));
				}
				
				return map;
			}
			@Override
			protected HashMap<Integer, List<Chap>> readValueIfFileNotExist() {
				return new HashMap<>();
			}
		};
	}

	private SerializeHelper<List<Data>> data_serialize() {
		return new SerializeHelper<List<Data>>(Paths.get("data")) {
			@Override
			protected ByteBuffer buffer(int size) {
				return buffer;
			}
			
			@Override
			protected void write(DataWriter w, List<Data> data) throws IOException  {
				w.writeInt(data.size());

				for (Data d : data) {
					w.writeInt(d.manga_id)
					.writeUTF(d.manga_name)
					.writeUTF(d.last_chapter);
				}
				System.out.println("cache write: "+p);
			}
			
			@Override
			protected List<Data> readValueIfFileNotExist() {
				return new ArrayList<>();
			}
			
			@Override
			protected List<Data> read(DataReader reader) throws IOException {
				int size = reader.readInt();
				ArrayList<Data> data = new ArrayList<>(size);
				
				data = new ArrayList<>(size);

				for (int i = 0; i < size; i++) 
					data.add(new Data(reader.readInt(), reader.readUTF(), reader.readUTF()));

				System.out.println("cache loaded: "+size+", path: "+p);
				return data;
			}
		};
	}

	private void setData() {
		table.getItems().addAll(data);
		search.setAllData(data);
	}
	private void prepareSearchTF(Stage stage) {
		stage.getScene().getAccelerators().put(combination(KeyCode.S, KeyCombination.CONTROL_DOWN), () -> searchTF.requestFocus());
		stage.getScene().getAccelerators().put(combination(KeyCode.C, KeyCombination.CONTROL_DOWN), () -> moveData());

		searchTF.textProperty().addListener(e -> search.set(searchTF.getText()));
		searchTF.setOnKeyReleased(e -> {
			if(e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
				if(table.getItems().isEmpty())
					return;

				int index = model.getSelectedIndex(); 
				if(index < 0)
					model.select(0);
				else if(e.getCode() == KeyCode.UP && index > 0)
					model.selectPrevious();
				else if(index < table.getItems().size())
					model.selectNext();
			}
			if(e.getCode() == KeyCode.C && e.isControlDown() && Checker.isEmpty(searchTF.getSelectedText()))
				moveData();  
		});
		search.setOnChange(() -> {
			model.clearSelection();
			search.applyFilter(table.getItems());
			Platform.runLater(() -> {
				if(!table.getItems().isEmpty())
					model.select(0);
			});
		});
	}

	private final String scrapper_cmd = System2.lookup("scrapper_cmd");

	@FXML
	public void copyAction(Event o) {
		String s = table2.getItems().stream().map(d -> String.valueOf(d.manga_id)).collect(Collectors.joining(" "));
		if(scrapper_cmd != null)
			s = scrapper_cmd.concat(s);
		FxClipboard.setString(s);
		System.out.println("copied: "+s);
	}
	private void moveData() {
		Data d = model.getSelectedItem();
		if(d == null)
			return;
		model.clearSelection();
		data.remove(d);
		table.getItems().remove(d);
		table2.getItems().add(d);
		search.setAllData(data);
	}
	private void prepareTable() {
		table(table);
		table(table2);

		model = table.getSelectionModel();
		model.selectedItemProperty().addListener(this);
		table2.getSelectionModel().selectedItemProperty().addListener(this);
	}

	@SuppressWarnings("unchecked")
	private void table(TableView<Data> table) {
		table.setEditable(false);

		TableColumn<Data, Integer> c1 = FxTable.column("manga_id", d -> d.manga_id);
		TableColumn<Data, String> c2 = FxTable.column("dirname", d -> d.manga_name);
		c2.setPrefWidth(200);
		c1.setPrefWidth(70);

		table.getColumns().addAll(c1, c2);
	}


	private Data current;

	@Override
	public void changed(ObservableValue<? extends Data> observable, Data oldValue, Data newValue) {
		lastChapT.setText(newValue == null ? null : newValue.last_chapter);
		this.current = newValue;
	}

	@SuppressWarnings("unchecked")
	private Stage createNew() {
		Stage stage = new Stage(StageStyle.UTILITY);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(this.stage);

		TableView<Chap> table = new TableView<>();

		table.setEditable(false);

		TableColumn<Chap, Double> c1 = FxTable.column("number", d -> d.number);
		TableColumn<Chap, String> c2 = FxTable.column("name", d -> d.name);
		c2.setPrefWidth(200);
		c1.setPrefWidth(50);

		table.getColumns().addAll(c1, c2);

		stage.setScene(new Scene(table));
		return stage;
	}

	final WeakAndLazy<Stage> stage2 = new WeakAndLazy<>(this::createNew);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@FXML
	private void allChapters(Event e) {
		Stage s = stage2.get();
		((TableView)s.getScene().getRoot())
		.getItems().setAll(current == null ? Collections.emptyList() : chapters(current.manga_id));
		s.show();
	}

	@Override
	public void stop() throws Exception {
		if(samdb != null) {
			samdb.close();
			System.out.println("db closed");

			if(lastChapsList != null && lastChapsList.size() != lastChapsList_size) 
				lastChapList_serialize.write(lastChapsList.isEmpty() ? null : lastChapsList);
		}
	}

	private SamrockDB samdb;

	private SamrockDB db() {
		if(samdb != null)
			return samdb;
		System.out.println("db loaded: "+Thread.currentThread().getStackTrace()[2]);
		return samdb = MyUtilsException.noError(() -> new SamrockDB());
	}

	private HashMap<Integer, List<Chap>> lastChapsList;
	private int lastChapsList_size;

	private List<Chap> chapters(int manga_id) {
		if(lastChapsList == null) {
			Path p = Paths.get("lastChapsList");
			if(Files.exists(p)) {
				try {
					lastChapsList = lastChapList_serialize.read();
					lastChapsList_size = lastChapsList.size(); 
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if(lastChapsList == null)
			lastChapsList = new HashMap<>();

		List<Chap> chaps = lastChapsList.get(manga_id);
		if(chaps != null)
			return chaps;

		try {
			chaps = db().collectToList("SELECT name, number from Chapters where manga_id = "+manga_id, Chap::new);
			chaps.sort(Comparator.<Chap>comparingDouble(d -> d.number).thenComparing(Comparator.comparing(d -> d.name)));
			lastChapsList.put(manga_id, chaps);
			return chaps;
		} catch (SQLException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	@FXML
	public void reload(Event obj) throws SQLException, ClassNotFoundException, IOException {
		data = db().collectToList("select manga_id, manga_name, name, max(number) from Mangas natural join Chapters group by manga_id", Data::new);
		data_serialize.write(data);

		reloadBtn.setVisible(false);

		if(lastChapsList != null) {
			lastChapsList.clear();
			lastChapsList_size = 0;
		}

		setData();
	}
}
