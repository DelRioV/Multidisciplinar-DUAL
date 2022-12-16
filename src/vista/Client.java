package vista;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import controlador.CreateDirectory;
import controlador.DeleteDirectory;
import controlador.DownloadFile;
import controlador.UploadEvent;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.*;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client extends JFrame {
    private static List<FTPFile> listFileDir;
    private static final long serialVersionUID = 1L;
    //Campos de cabecera parte superior
    private static JLabel serverLabel = new JLabel();
    private static JLabel userLabel = new JLabel();
    private static JLabel rootLabel = new JLabel();
    //Campos de mensajes parte inferior
    private static JLabel directoryTree = new JLabel();
    private static JLabel field = new JLabel();
    //Botones
    JButton btnUpload = new JButton("Subir fichero");
    JButton btnDownload = new JButton("Descargar fichero");
    JButton btnDelFile = new JButton("Eliminar fichero");
    JButton btnCreateDir = new JButton("Crear carpeta");
    JButton btnDelDir = new JButton("Eliminar carpeta");
    JButton btnExit = new JButton("Salir");

    JButton btnRenameFile = new JButton("Renombrar fichero");

    //Lista para los datos del directorio
    private static JList listDirec = new JList();
    //contenedor
    private final Container c = getContentPane();
    //Datos del servidor FTP - Servidor local
    private static FTPClient client = new FTPClient();//cliente FTP
    private String server = "127.0.0.1";
    private String user = "pablo";
    private String pasw = "1234";
    private boolean login;
    static String direclnicial = "/";
    //para saber el directorio y fichero seleccionado
    private static String direcSelec = direclnicial;
    private static String fileSelec = "";

    public Client() throws IOException {
        super("CLIENTE BÁSICO FTP");
        //para ver los comandos que se originan
        getClient().addProtocolCommandListener(new PrintCommandListener
                (new PrintWriter(System.out)));
        getClient().connect(getServer()); //conexión al servidor
        setLogin(getClient().login(getUser(), getPasw()));
        //Se establece el directorio de trabajo actual
        getClient().changeWorkingDirectory(direclnicial);
        //Oteniendo ficheros y directorios del directorio actual
        FTPFile[] files = getClient().listFiles();
        //Construyendo la lista de ficheros y directorios
        //del directorio de trabajo actual
        fillList(files, direclnicial);
        //preparar campos de pantalla
        getDirectoryTree().setText("« ARBOL DE DIRECTORIOS CONSTRUIDO »");
        getServerLabel().setText("Servidor FTP: " + getServer());
        getUserLabel().setText("Usuario: " + getUser());
        getRootLabel().setText("DIRECTORIO RAIZ: " + direclnicial);
        //Preparación de la lista
        //se configura el tipo de selección para que solo se pueda
        //seleccionar un elemento de la lista
        getListDirec().setSelectionMode(ListSelectionModel.
                SINGLE_SELECTION);
        //barra de desplazamiento para la lista
        JScrollPane barraDesplazamiento = new
                JScrollPane(getListDirec());
        barraDesplazamiento.setPreferredSize
                (new Dimension(335, 420));
        barraDesplazamiento.setBounds(new Rectangle
                (5, 65, 335, 420));
        c.add(barraDesplazamiento);
        c.setLayout(new FlowLayout());
        //se añaden el resto de los campos de pantalla
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(510, 600);
        //ACCIONES AL PULSAR en la lista o en los botones
        getListDirec().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    setFileSelec("");
                    //elemento que se ha seleccionado de la lista
                    String fic = getListDirec().getSelectedValue().toString();

                    if (getListDirec().getSelectedIndex() == 0) {
                        //Se hace clic en el primer elemento del JList
                        //se comprueba si es el directorio inicial
                        if (!fic.equals(direclnicial)) {
                            //si no estamos en el dictorio inicial, hay que
                            //subir al directorio padre
                            try {
                                getClient().changeToParentDirectory();
                                setDirecSelec(getClient().printWorkingDirectory());
                                FTPFile[] ff2 = null;
                                //directorio de trabajo actual=directorio padre
                                getClient().changeWorkingDirectory(getDirecSelec());
                                //se obtienen ficheros y directorios
                                ff2 = getClient().listFiles();
                                getDirectoryTree().setText("");
                                //se llena la lista con fich. del directorio padre
                                fillList(ff2, getDirecSelec());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    } //No se hace clic en el primer elemento del JList
                    //Puede ser un fichero o un directorio
                    else {
                        try{
                        if (fic.substring(0, 6).equals("(DIR) ")) {
                            //SE TRATA DE UN DIRECTORIO
                            try {
                                fic = fic.substring(6);
                                String direcSelec2 = "";
                                if (getDirecSelec().equals("/"))
                                    direcSelec2 = getDirecSelec() + fic;
                                else
                                    direcSelec2 = getDirecSelec() + "/" + fic;
                                FTPFile[] ff2 = null;
                                getClient().changeWorkingDirectory(direcSelec2);
                                ff2 = getClient().listFiles();
                                getDirectoryTree().setText("DIRECTORIO: " + fic + ", "
                                        + ff2.length + " elementos");
                                //directorio actual
                                setDirecSelec(direcSelec2);
                                //se llena la lista con datos del directorio
                                fillList(ff2, getDirecSelec());
                            } catch (IOException e2) {
                                e2.printStackTrace();
                            }
                        }

                        } catch (Exception ex){

                        }
                        setFileSelec(listFileDir.get(listDirec.getSelectedIndex() - 1).getName());
                    }
                }
            }
        });
        btnDelDir.addActionListener(new DeleteDirectory(client, field));
        btnCreateDir.addActionListener(new CreateDirectory(client, field));
        btnDownload.addActionListener(new DownloadFile(client));
        btnUpload.addActionListener(new UploadEvent());
        add(getServerLabel());
        add(getUserLabel());
        add(getRootLabel());
        add(getDirectoryTree());
        add(field);
        add(btnCreateDir);
        add(btnDelDir);
        add(btnUpload);
        add(btnDownload);
        add(btnDelFile);
        add(btnRenameFile);
        add(btnExit);
        setVisible(true);

    }

    public void fillList(FTPFile[] files, String direc2) {
        if (files == null) return;
        //se crea un objeto DefaultListModel
        DefaultListModel modeloLista = new DefaultListModel();
        modeloLista = new DefaultListModel();
        //se definen propiedades para la lista, color y tipo de fuente
        getListDirec().setForeground(Color.blue);
        Font fuente = new Font("Courier", Font.PLAIN, 12);
        getListDirec().setFont(fuente);
        //se eliminan los elementos de la lista
        getListDirec().removeAll();
        try {
            //se establece el directorio de trabajo actual
            getClient().changeWorkingDirectory(direc2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setDirecSelec(direc2); //directorio actual
        //se añade el directorio de trabajo al listmodel, primer elemento
        modeloLista.addElement(direc2);
        //se recorre el array con los ficheros y directorios
        for (int i = 0; i < files.length; i++) {
            if (!(files[i].getName()).equals(".")
                    && !(files[i].getName()).equals("..")) {
                //nos saltamos los directorios . y ..
                //Se obtiene el nombre del fichero o directorio
                String f = files[i].getName();
                //Si es diretorio se añade al nombre (DIR)
                if (files[i].isDirectory()) f = "(DIR) " + f;
                //se añade el nombre del fie o direc al listmodel
                modeloLista.addElement(f);
            } //fin if
        }// fin for
        try {
            //se asigna el listmodel al JList,
            //se muestra en pantalla la lista de ficheros y direc
            getListDirec().setModel(modeloLista);
            setListFileDir(Arrays.asList(getClient().listFiles()));
        } catch (NullPointerException n) {
            ; //Se produce al cambiar de directorio
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) throws IOException {
        new Client();
    }

    public static JLabel getServerLabel() {
        return serverLabel;
    }

    public static void setServerLabel(JLabel serverLabel) {
        Client.serverLabel = serverLabel;
    }

    public static JLabel getUserLabel() {
        return userLabel;
    }

    public static void setUserLabel(JLabel userLabel) {
        Client.userLabel = userLabel;
    }

    public static JLabel getRootLabel() {
        return rootLabel;
    }

    public static void setRootLabel(JLabel rootLabel) {
        Client.rootLabel = rootLabel;
    }

    public static JLabel getDirectoryTree() {
        return directoryTree;
    }

    public static void setDirectoryTree(JLabel directoryTree) {
        Client.directoryTree = directoryTree;
    }

    public static JList getListDirec() {
        return listDirec;
    }

    public static void setListDirec(JList listDirec) {
        Client.listDirec = listDirec;
    }

    public static FTPClient getClient() {
        return client;
    }

    public static void setClient(FTPClient client) {
        Client.client = client;
    }

    public static String getDirecSelec() {
        return direcSelec;
    }

    public static void setDirecSelec(String direcSelec) {
        Client.direcSelec = direcSelec;
    }

    public static String getFileSelec() {
        return fileSelec;
    }

    public static void setFileSelec(String fileSelec) {
        Client.fileSelec = fileSelec;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPasw() {
        return pasw;
    }

    public void setPasw(String pasw) {
        this.pasw = pasw;
    }

    public boolean isLogin() {
        return login;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }


    public List<FTPFile> getListFileDir() {
        return listFileDir;
    }

    public void setListFileDir(List<FTPFile> listFileDir) {
        this.listFileDir = listFileDir;
    }
}

