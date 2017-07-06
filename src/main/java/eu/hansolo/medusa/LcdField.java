/*
 * Copyright (c) 2015-2017 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.hansolo.medusa;

import java.util.Locale;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

/**
 * LCDField as a reusable Skinelement e.g. as used in AmpSkin
 *
 */
public class LcdField {
  private Rectangle lcd;
  private Label lcdText;

  public Rectangle getLcd() {
    return lcd;
  }

  public void setLcd(Rectangle lcd) {
    this.lcd = lcd;
  }

  public Label getLcdText() {
    return lcdText;
  }

  public void setLcdText(Label lcdText) {
    this.lcdText = lcdText;
  }

  /**
   * construct me from a width and height
   * 
   * @param preferredWidth
   * @param preferredHeight
   * @param visible
   */
  public LcdField(double value, Locale locale, String format,
      double preferredWidth, double preferredHeight, boolean visible) {
    setLcd(new Rectangle(0.3 * preferredWidth, 0.1 * preferredHeight));
    getLcd().setArcWidth(0.0125 * preferredHeight);
    getLcd().setArcHeight(0.0125 * preferredHeight);
    getLcd().relocate((preferredWidth - getLcd().getWidth()) * 0.5,
        0.44 * preferredHeight);

    setLcdText(new Label(String.format(locale, format, value)));
    getLcdText().setAlignment(Pos.CENTER_RIGHT);
    getLcdText().setVisible(visible);
  }

  /**
   * resize the LcdField
   * 
   * @param width
   * @param height
   * @param lcdFont
   * @param visible
   */
  public void resize(double width, double height, LcdFont lcdFont,
      boolean visible) {
    if (visible) {
      lcdText.setPadding(new Insets(0, 0.005 * width, 0, 0.005 * width));

      switch (lcdFont) {
      case LCD:
        lcdText.setFont(Fonts.digital(0.108 * height));
        lcdText.setTranslateY(0.45 * height);
        break;
      case DIGITAL:
        lcdText.setFont(Fonts.digitalReadout(0.105 * height));
        lcdText.setTranslateY(0.44 * height);
        break;
      case DIGITAL_BOLD:
        lcdText.setFont(Fonts.digitalReadoutBold(0.105 * height));
        lcdText.setTranslateY(0.44 * height);
        break;
      case ELEKTRA:
        lcdText.setFont(Fonts.elektra(0.1116 * height));
        lcdText.setTranslateY(0.435 * height);
        break;
      case STANDARD:
      default:
        lcdText.setFont(Fonts.robotoMedium(0.09 * height));
        lcdText.setTranslateY(0.43 * height);
        break;
      }
      lcdText.setAlignment(Pos.CENTER_RIGHT);
      lcdText.setPrefSize(0.3 * width, 0.014 * height);
      lcdText.setTranslateX((width - lcdText.getPrefWidth()) * 0.5);

    } else {
      lcdText.setAlignment(Pos.CENTER);
      lcdText.setFont(Fonts.robotoMedium(height * 0.1));
      lcdText.setPrefSize(0.3 * width, 0.014 * height);
      lcdText.setTranslateY(0.43 * height);
      lcdText
          .setTranslateX((width - lcdText.getLayoutBounds().getWidth()) * 0.5);
    }

  }

  /**
   * set the Value
   * 
   * @param width
   * @param locale
   * @param formatString
   * @param currentValue
   * @param visible
   */
  public void setValue(double width, Locale locale, String formatString,
      double currentValue, boolean visible) {
    lcdText.setText((String.format(locale, formatString, currentValue)));
    if (visible) {
      lcdText.setAlignment(Pos.CENTER_RIGHT);
      lcdText.setTranslateX((width - lcdText.getPrefWidth()) * 0.5);
    } else {
      lcdText.setAlignment(Pos.CENTER);
      lcdText
          .setTranslateX((width - lcdText.getLayoutBounds().getWidth()) * 0.5);
    }
  }

  /**
   * resize based on width and height
   * 
   * @param width
   * @param height
   */
  public void resize(double width, double height) {
    lcd.setWidth(0.3 * width);
    lcd.setHeight(0.1 * height);
    lcd.setArcWidth(0.0125 * height);
    lcd.setArcHeight(0.0125 * height);
    lcd.relocate((width - lcd.getWidth()) * 0.5, 0.44 * height);
  }

  /**
   * redraw me
   * @param visible
   * @param lcdDesign
   */
  public void redraw(boolean visible, LcdDesign lcdDesign) {
    if (visible) {
      Color[] lcdColors = lcdDesign.getColors();
      LinearGradient lcdGradient = new LinearGradient(0, 1, 0,
          lcd.getHeight() - 1, false, CycleMethod.NO_CYCLE,
          new Stop(0, lcdColors[0]), new Stop(0.03, lcdColors[1]),
          new Stop(0.5, lcdColors[2]), new Stop(0.5, lcdColors[3]),
          new Stop(1.0, lcdColors[4]));
      Paint lcdFramePaint;
      if (lcdDesign.name().startsWith("FLAT")) {
        lcdFramePaint = Color.WHITE;
      } else {
        lcdFramePaint = new LinearGradient(0, 0, 0, lcd.getHeight(), false,
            CycleMethod.NO_CYCLE, new Stop(0.0, Color.rgb(26, 26, 26)),
            new Stop(0.01, Color.rgb(77, 77, 77)),
            new Stop(0.99, Color.rgb(77, 77, 77)),
            new Stop(1.0, Color.rgb(221, 221, 221)));
      }
      lcd.setFill(lcdGradient);
      lcd.setStroke(lcdFramePaint);

      lcdText.setTextFill(lcdColors[5]);
    }

  }

}
