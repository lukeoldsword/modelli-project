import java.awt.Dimension;
import java.awt.FlowLayout;
import java.net.MalformedURLException;

import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamCompositeDriver;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamDevice;
import com.github.sarxos.webcam.ds.ipcam.IpCamDeviceRegistry;
import com.github.sarxos.webcam.ds.ipcam.IpCamDriver;
import com.github.sarxos.webcam.ds.ipcam.IpCamMode;


/**
 * Example of how to use internal webcam together with external IP cameras.
 * 
 * @author Bartosz Firyn (SarXos)
 */
public class DualNativeAndMjpegWebcamExample {

	/**
	 * Customized webcam driver.
	 */
	protected static class MyCompositeDriver extends WebcamCompositeDriver {

		public MyCompositeDriver() {
			add(new WebcamDefaultDriver());
			add(new IpCamDriver());
		}
	}

	// register custom composite driver
	static {
		Webcam.setDriver(new MyCompositeDriver());
	}

	public static void main(String[] args) throws MalformedURLException {

		// register IP camera device
		String ip1 =  "\"88";
		String ip2 = ".37";
		String ip3 = ".116";
		String ip4 = ".138\"";
		
		IpCamDeviceRegistry.register(new IpCamDevice("Lignano Beach","http://"+ip1+ip2+ip3+ip4+"/mjpg/video.mjpg", IpCamMode.PUSH));

		JFrame window = new JFrame("Live Views From Lignano Beach (Italy)");
		window.setLayout(new FlowLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		for (Webcam webcam : Webcam.getWebcams()) {
			webcam.setViewSize(new Dimension(352, 288));
			window.add(new WebcamPanel(webcam));
		}

		window.pack();
		window.setVisible(true);
	}
}
