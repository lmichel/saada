package saadadb.admintool.dnd;

/*
Java Swing, 2nd Edition
By Marc Loy, Robert Eckstein, Dave Wood, James Elliott, Brian Cole
ISBN: 0-596-00408-7
Publisher: O'Reilly 
*/
// GestureTest.java
//A simple (?) test of the DragGesture classes to see if we
//can recognize a simple drag gesture.
//

import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * @author laurentmichel
 * * @version $Id: GestureTest.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class GestureTest extends JFrame implements DragGestureListener {

  DragSource ds;

  JList jl;

  String[] items = { "Java", "C", "C++", "Lisp", "Perl", "Python" };

  public GestureTest() {
    super("Gesture Test");
    setSize(200, 150);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
        System.exit(0);
      }
    });
    jl = new JList(items);
    jl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    getContentPane().add(new JScrollPane(jl), BorderLayout.CENTER);

    ds = new DragSource();
    DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer(jl,
        DnDConstants.ACTION_COPY, this);
    setVisible(true);
  }

  public void dragGestureRecognized(DragGestureEvent dge) {
  }

  public static void main(String args[]) {
    new GestureTest();
  }
}
