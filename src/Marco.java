import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Marco extends JFrame {
    public Marco(int f, int c, MemoriaPrincipal mem) {
        super("RESULTADOS");
        Object[][] datosMemoria = new Object[f][c];
        for(int i = 0; i < f; i++) {
            for(int j = 0; j < c; j++) {
                datosMemoria[i][j] = String.valueOf(mem.memDatos[i].palabra[j]);
            }
        }
        JTable tabla = new JTable(datosMemoria, null);
        tabla.setPreferredScrollableViewportSize(new Dimension(500, 100));
        JScrollPane scrollPane = new JScrollPane(tabla);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
            }
        });
    }
}
