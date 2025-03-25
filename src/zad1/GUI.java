package zad1;

import com.formdev.flatlaf.FlatDarculaLaf;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class GUI {
    private JButton _choseCountry;
    private JButton _choseCityButton;
    private JButton _rateButton;
    private JButton _NBPButton;
    private JLabel _weatherLabel;
    private final Service _s;
    private final JFrame _frame;
    private Set<String> _countries;


    public GUI(Service s) {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("xd");
        }
        _s=s;
        _frame = new JFrame("");
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        _frame.setSize(600, 400);
        _frame.setLayout(new FlowLayout());
        _frame.setLocationRelativeTo(null);

        _choseCountry.addActionListener(this::choseCountryListener);
        _choseCityButton.addActionListener(this::choseCityListener);
        _rateButton.addActionListener(this::rateButtonListener);
        _NBPButton.addActionListener(this::NBPrateListener);

        _choseCityButton.setEnabled(false);
        _rateButton.setEnabled(false);
        _NBPButton.setEnabled(false);

        _frame.add(_choseCountry);
        _frame.add(_choseCityButton);
        _frame.add(_rateButton);
        _frame.setVisible(true);
        _frame.add(_NBPButton);


    }

    private void choseCountryListener(ActionEvent e) {

        JComboBox<String> choseCountryComboBox = new JComboBox<>(getCountries());
        int res = JOptionPane.showConfirmDialog(
                _frame,
                choseCountryComboBox,
                "Select a Country",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (res == JOptionPane.OK_OPTION) {
            _s.setCountry((String) choseCountryComboBox.getSelectedItem());
            _choseCityButton.setEnabled(true);
            _rateButton.setEnabled(true);
            _NBPButton.setEnabled(true);
        }
    }

    private void choseCityListener(ActionEvent e) {
        JComboBox<String> choseCityComboBox = new JComboBox<>(_s.getCities());
        int res = JOptionPane.showConfirmDialog(
                _frame,
                choseCityComboBox,
                "Select a City",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (res == JOptionPane.OK_OPTION) {
            String city = Objects.requireNonNull(choseCityComboBox.getSelectedItem()).toString();
            handleWeatherLabel(city);
            new JFXPanel();
            Platform.runLater(() -> {
                WebView webView = new WebView();
                WebEngine webEngine = webView.getEngine();
                webEngine.load("https://en.wikipedia.org/wiki/"+city);
                System.out.println(webEngine);
            });
        }
    }

    private void rateButtonListener(ActionEvent e) {
        JComboBox<String> choseCountryComboBox = new JComboBox<>(getCountries());
        int res = JOptionPane.showConfirmDialog(
                _frame,
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
                Currency currency = Currency.getInstance(_s.getLocaleFromCountryName(country));
                exchangeRate = _s.getRateFor(currency.getSymbol());
                message = "1 " + _s.getCurrency().getSymbol() + " = " + exchangeRate + " " + currency.getSymbol();
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
            message = "1 " + _s.getCurrency().getSymbol() + " = " + _s.getNBPRate() + " PLN";

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
            _s.getWeather(city);
        } catch (Exception e1) {
            try {
                _frame.remove(_weatherLabel);
            } catch (Exception ex) {
                //nothing to remove
            }
            _weatherLabel = new JLabel("sry, city not found :(");
            _frame.add(_weatherLabel);
            _frame.revalidate();
            _frame.repaint();
            return;
        }
        ImageIcon weatherIcon = _s.getWeatherIcon();
        double temperature = _s.getTemperature();

        try {
            _frame.remove(_weatherLabel);
        } catch (Exception ex) {
            //nothing to remove
        }
        String labelText = city + " <font size='6'>" + temperature + "°C</font>";

        _weatherLabel = new JLabel("<html>" + labelText + "</html>");
        _weatherLabel.setIcon(weatherIcon);
        _frame.add(_weatherLabel);
        _frame.revalidate();
        _frame.repaint();
    }

    private String[] getCountries() {
        if (_countries != null)
            return _countries.toArray(new String[0]);
        _countries = new TreeSet<>();
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            String country = locale.getDisplayCountry();
            if (!country.isEmpty()) {
                _countries.add(country);
            }
        }
        return _countries.toArray(new String[0]);
    }
}
