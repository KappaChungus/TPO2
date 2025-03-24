/**
 *
 *  @author Ebing Jan S31236
 *
 */

package zad1;

import javax.swing.*;

public class Main {
  public static void main(String[] args) {

    Service s = new Service("Poland");
    String weatherJson = s.getWeather("Warsaw");
    Double rate1 = s.getRateFor("USD");
    Double rate2 = s.getNBPRate();

    SwingUtilities.invokeLater(()->new GUI(s));
  }
}
