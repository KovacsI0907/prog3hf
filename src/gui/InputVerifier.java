package gui;

/**
 * Egyszerű interfész, ami megad egy verify, action és falseAction metódust
 * Egy VerifierDocumentListenerre rátéve, egy adott input változásakor
 * meghívódik verify() és ha igaz, akkor action() ha hamis akkor pedig falseAction() hajtódik végre
 */
public interface InputVerifier {
    public boolean verify();
    public void action();
    public void falseAction();
}
