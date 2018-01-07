import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.gstreamer.GStreamerDriver;


/**
 * Example of how to take single picture.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class TakePictureExample2 {

	static {
		Webcam.setDriver(new GStreamerDriver());
	}

	public static void main(String[] args) {

		final Webcam webcam = Webcam.getDefault();
		webcam.open();

		JFrame window = new JFrame();
		JButton button = new JButton(new AbstractAction("Take Snapshot Now") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String name = String.format("test-%d.jpg", System.currentTimeMillis());
					ImageIO.write(webcam.getImage(), "JPG", new File(name));
					System.out.println("Fil " + name + " has been saved"); 
				} catch (IOException t) {
					System.out.println("Operation of writing an image to file failed or interrupted");
				}
			}
		});

		window.add(button);

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.pack();
		window.setVisible(true);

	}
}
