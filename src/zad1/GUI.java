package zad1;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class GUI {
    private JButton choseCountry;
    private JButton choseCityButton;
    private JButton rateButton;
    private JButton NBPButton;
    private JLabel weatherLabel;
    private Service s;
    private final JFrame frame;
    private Set<String> countries;


    public GUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("xd");
        }
        frame = new JFrame("");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new FlowLayout());
        frame.setLocationRelativeTo(null);

        choseCountry.addActionListener(this::choseCountryListener);
        choseCityButton.addActionListener(this::choseCityListener);
        rateButton.addActionListener(this::rateButtonListener);
        NBPButton.addActionListener(this::NBPrateListener);

        choseCityButton.setEnabled(false);
        rateButton.setEnabled(false);
        NBPButton.setEnabled(false);

        frame.add(choseCountry);
        frame.add(choseCityButton);
        frame.add(rateButton);
        frame.setVisible(true);
        frame.add(NBPButton);


    }

    private void choseCountryListener(ActionEvent e) {

        JComboBox<String> choseCountryComboBox = new JComboBox<>(getCountries());
        int res = JOptionPane.showConfirmDialog(
                frame,
                choseCountryComboBox,
                "Select a Country",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (res == JOptionPane.OK_OPTION) {
            s= Service.getInstance(Objects.requireNonNull(choseCountryComboBox.getSelectedItem()).toString());
            choseCityButton.setEnabled(true);
            rateButton.setEnabled(true);
            NBPButton.setEnabled(true);
        }
    }

    private void choseCityListener(ActionEvent e) {
        JComboBox<String> choseCityComboBox = new JComboBox<>(s.getCities());
        int res = JOptionPane.showConfirmDialog(
                frame,
                choseCityComboBox,
                "Select a City",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (res == JOptionPane.OK_OPTION) {
            String city = Objects.requireNonNull(choseCityComboBox.getSelectedItem()).toString();
            handleWeatherLabel(city);

        }
    }

    private void rateButtonListener(ActionEvent e) {
        JComboBox<String> choseCountryComboBox = new JComboBox<>(getCountries());
        int res = JOptionPane.showConfirmDialog(
                frame,
                choseCountryComboBox,
                "Select a Country",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (res == JOptionPane.OK_OPTION) {

            String message;
            double exchangeRate;
            try {
                String country = Objects.requireNonNull(choseCountryComboBox.getSelectedItem()).toString();
                Currency currency = Currency.getInstance(s.getLocaleFromCountryName(country));
                exchangeRate = s.getRateFor(currency.getSymbol());
                message = "1 " + s.getCurrency().getSymbol() + " = " + exchangeRate + " " + currency.getSymbol();
            } catch (RuntimeException e2) {
                message = e2.getMessage();
            }
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    "Exchange rate",
                    JOptionPane.INFORMATION_MESSAGE);

        }

    }

    private void NBPrateListener(ActionEvent e) {
        String message;
        try {
            message = "1 " + s.getCurrency().getSymbol() + " = " + s.getNBPRate() + " PLN";

        } catch (RuntimeException e1) {
            message = e1.getMessage();
        }
        JOptionPane.showMessageDialog(
                null,
                message,
                "NBP exchange rate",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleWeatherLabel(String city) {
        try {
            s.getWeather(city);
        } catch (Exception e1) {
            try {
                frame.remove(weatherLabel);
            } catch (Exception ex) {
                //nothing to remove
            }
            weatherLabel = new JLabel("sry, city not found :(");
            frame.add(weatherLabel);
            frame.revalidate();
            frame.repaint();
            return;
        }
        ImageIcon weatherIcon = s.getWeatherIcon();
        double temperature = s.getTemperature();

        try {
            frame.remove(weatherLabel);
        } catch (Exception ex) {
            //nothing to remove
        }
        String labelText = city + " <font size='6'>" + temperature + "°C</font>";

        weatherLabel = new JLabel("<html>" + labelText + "</html>");
        weatherLabel.setIcon(weatherIcon);
        frame.add(weatherLabel);
        frame.revalidate();
        frame.repaint();
    }

    private String[] getCountries() {
        if (countries != null)
            return countries.toArray(new String[0]);
        countries = new TreeSet<>();
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (!country.isEmpty()) {
                countries.add(country);
            }
        }
        return countries.toArray(new String[0]);
    }
}
