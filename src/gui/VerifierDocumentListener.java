package gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class VerifierDocumentListener implements DocumentListener {
    public final InputVerifier verifier;
    public VerifierDocumentListener(InputVerifier verifier){
        this.verifier = verifier;
    }
    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        if(verifier.verify()){
            verifier.action();
        }else{
            verifier.falseAction();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        if(verifier.verify()){
            verifier.action();
        }else {
            verifier.falseAction();
        }
    }

    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        if(verifier.verify()){
            verifier.action();
        }else{
            verifier.falseAction();
        }
    }
}
