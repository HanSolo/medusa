/*
 * Copyright (c) 2015 by Gerrit Grunwald
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

import javafx.scene.text.Font;


/**
 * Created by hansolo on 11.12.15.
 */
public class Fonts {
    private static final String DIGITAL_NAME;
    private static final String DIGITAL_READOUT_NAME;
    private static final String DIGITAL_READOUT_BOLD_NAME;
    private static final String ELEKTRA_NAME;
    private static final String ROBOTO_THIN_NAME;
    private static final String ROBOTO_LIGHT_NAME;
    private static final String ROBOTO_REGULAR_NAME;
    private static final String ROBOTO_MEDIUM_NAME;
    private static final String ROBOTO_BOLD_NAME;
    private static final String ROBOTO_LIGHT_CONDENSED_NAME;
    private static final String ROBOTO_REGULAR_CONDENSED_NAME;
    private static final String ROBOTO_BOLD_CONDENSED_NAME;
    private static final String LATO_LIGHT_NAME;
    private static final String LATO_REGULAR_NAME;
    private static final String LATO_BOLD_NAME;

    private static String digitalName;
    private static String digitalReadoutName;
    private static String digitalReadoutBoldName;
    private static String elektraName;

    private static String robotoThinName;
    private static String robotoLightName;
    private static String robotoRegularName;
    private static String robotoMediumName;
    private static String robotoBoldName;

    private static String robotoLightCondensedName;
    private static String robotoRegularCondensedName;
    private static String robotoBoldCondensedName;

    private static String latoLightName;
    private static String latoRegularName;
    private static String latoBoldName;


    static {
        try {
            digitalName                = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/digital.ttf"), 10).getName();
            digitalReadoutName         = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/digitalreadout.ttf"), 10).getName();
            digitalReadoutBoldName     = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/digitalreadoutb.ttf"), 10).getName();
            elektraName                = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/elektra.ttf"), 10).getName();
            robotoThinName             = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/Roboto-Thin.ttf"), 10).getName();
            robotoLightName            = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/Roboto-Light.ttf"), 10).getName();
            robotoRegularName          = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/Roboto-Regular.ttf"), 10).getName();
            robotoMediumName           = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/Roboto-Medium.ttf"), 10).getName();
            robotoBoldName             = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/Roboto-Bold.ttf"), 10).getName();
            robotoLightCondensedName   = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/RobotoCondensed-Light.ttf"), 10).getName();
            robotoRegularCondensedName = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/RobotoCondensed-Regular.ttf"), 10).getName();
            robotoBoldCondensedName    = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/RobotoCondensed-Bold.ttf"), 10).getName();
            latoLightName              = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/Lato-Lig.otf"), 10).getName();
            latoRegularName            = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/Lato-Reg.otf"), 10).getName();
            latoBoldName               = Font.loadFont(Fonts.class.getResourceAsStream("/eu/hansolo/medusa/Lato-Bol.otf"), 10).getName();
        } catch (Exception exception) { }
        DIGITAL_NAME                  = digitalName;
        DIGITAL_READOUT_NAME          = digitalReadoutName;
        DIGITAL_READOUT_BOLD_NAME     = digitalReadoutBoldName;
        ELEKTRA_NAME                  = elektraName;
        ROBOTO_THIN_NAME              = robotoThinName;
        ROBOTO_LIGHT_NAME             = robotoLightName;
        ROBOTO_REGULAR_NAME           = robotoRegularName;
        ROBOTO_MEDIUM_NAME            = robotoMediumName;
        ROBOTO_BOLD_NAME              = robotoBoldName;
        ROBOTO_LIGHT_CONDENSED_NAME   = robotoLightCondensedName;
        ROBOTO_REGULAR_CONDENSED_NAME = robotoRegularCondensedName;
        ROBOTO_BOLD_CONDENSED_NAME    = robotoBoldCondensedName;
        LATO_LIGHT_NAME               = latoLightName;
        LATO_REGULAR_NAME             = latoRegularName;
        LATO_BOLD_NAME                = latoBoldName;
    }


    // ******************** Methods *******************************************
    public static Font digital(final double SIZE) { return new Font(DIGITAL_NAME, SIZE); }

    public static Font digitalReadout(final double SIZE) { return new Font(DIGITAL_READOUT_NAME, SIZE); }
    public static Font digitalReadoutBold(final double SIZE) { return new Font(DIGITAL_READOUT_BOLD_NAME, SIZE); }

    public static Font elektra(final double SIZE) { return new Font(ELEKTRA_NAME, SIZE); }

    public static Font robotoThin(final double SIZE) { return new Font(ROBOTO_THIN_NAME, SIZE); }
    public static Font robotoLight(final double SIZE) { return new Font(ROBOTO_LIGHT_NAME, SIZE); }
    public static Font robotoRegular(final double SIZE) { return new Font(ROBOTO_REGULAR_NAME, SIZE); }
    public static Font robotoMedium(final double SIZE) { return new Font(ROBOTO_MEDIUM_NAME, SIZE); }
    public static Font robotoBold(final double SIZE) { return new Font(ROBOTO_BOLD_NAME, SIZE); }

    public static Font robotoCondensedLight(final double SIZE) { return new Font(ROBOTO_LIGHT_CONDENSED_NAME, SIZE); }
    public static Font robotoCondensedRegular(final double SIZE) { return new Font(ROBOTO_REGULAR_CONDENSED_NAME, SIZE); }
    public static Font robotoCondensedBold(final double SIZE) { return new Font(ROBOTO_BOLD_CONDENSED_NAME, SIZE); }

    public static Font latoLight(final double SIZE) { return new Font(LATO_LIGHT_NAME, SIZE); }
    public static Font latoRegular(final double SIZE) { return new Font(LATO_REGULAR_NAME, SIZE); }
    public static Font latoBold(final double SIZE) { return new Font(LATO_BOLD_NAME, SIZE); }
}
