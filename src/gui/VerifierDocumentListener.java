package gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Ez az osztály megkönnyíti a inputok validációját.
 * Felhasználja az InputVerifier interfészt.
 * A verifier pozitív illetve negatív kimeneteire végrehajt akciókat, amik szintén a verifierben vannak megadva.
 * Ezt az osztályt rá lehet tenni pl. JTextFieldek documentumaira listenerként (input változáskor hívódik)
 */
public class VerifierDocumentListener implements DocumentListener {
    public final InputVerifier verifier;
    public VerifierDocumentListener(InputVerifier verifier){
        this.verifier = verifier;
    }

    /**
     * Változáskor meghívja a verifier actionjét
     * @param documentEvent
     */
    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        if(verifier.verify()){
            verifier.action();
        }else{
            verifier.falseAction();
        }
    }

    /**
     * Változáskor meghívja a verifier actionjét
     * @param documentEvent
     */
    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        if(verifier.verify()){
            verifier.action();
        }else {
            verifier.falseAction();
        }
    }

    /**
     * Változáskor meghívja a verifier actionjét
     * @param documentEvent
     */
    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        if(verifier.verify()){
            verifier.action();
        }else{
            verifier.falseAction();
        }
    }
}
