package ATCS;

import javax.swing.*;

/**
 * Created with IntelliJ IDEA.
 * User: Elod-Arpad
 * Date: 11/3/13
 * Time: 2:59 PM
 */
public class FuelVerifier extends InputVerifier{


        public boolean verify(JComponent input) {
            JTextField tf = (JTextField) input;
            try {
                if (Integer.parseInt(tf.getText()) > 0)
                    return true;
                else {
                    tf.setText(Integer.toString(Airport.getInstance().getFuelForDepartingPlanes()));
                    return false;
                }
            } catch (NumberFormatException e) {
                tf.setText(Integer.toString(Airport.getInstance().getFuelForDepartingPlanes()));
                return false;
            }
        }

}
