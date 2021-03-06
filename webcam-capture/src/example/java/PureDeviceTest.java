import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;

/**
 * PureDeviceTest
 */
public class PureDeviceTest {

	public static void main(String[] args) {

		WebcamDefaultDriver driver = new WebcamDefaultDriver();
		List<WebcamDevice> devices = driver.getDevices();
		WebcamDevice d1 = null;
		try {
			for (WebcamDevice d : devices) {
				d1 = d;
				System.out.println(d.getName());
				d.open();
				BufferedImage image = d.getImage();
				ImageIO.write(image, "jpg", new File(System.currentTimeMillis() + ".jpg"));
			}
		} catch (IOException e) {
			System.out.println("Operation of writing an image to file failed or interrupted");
		} finally {
			d1.close();
		}

		// finally at the end, don't forget to dispose
		for (WebcamDevice d : devices) {
			d.dispose();
		}
	}

}
