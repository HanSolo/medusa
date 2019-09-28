module eu.hansolo.tilesfx {

    // Java
    requires java.base;

    // Java-FX
    requires transitive javafx.base;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;

    exports eu.hansolo.medusa;
    exports eu.hansolo.medusa.skins;
    exports eu.hansolo.medusa.events;
    exports eu.hansolo.medusa.tools;
}