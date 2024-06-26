package com.jazz.engine.player;

import com.jazz.engine.Node;

/**
 * Human players use their external "algorithm".
 * We must wait until the move is received from the UI controller. A lock is employed for this purpose.
 */
public class HumanPlayer implements Player{
/*    @Override
    public int getMove(Node currentGameNode) {
        // Wait for a response from the GUI.
        Object lock = UIController.getInstance().getUserMoveReceivedLock();
        synchronized (lock){
            try{
                while (UIController.getInstance().getStoredMove()==0){
                    lock.wait();
                }
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }

        int move = UIController.getInstance().getStoredMove();
        //int moverColor = Move.getColor(move);

        //move = Move.setPrevEP(move,currentGameNode.getBitboards().enPassantable[moverColor^1]);
        UIController.getInstance().resetStoredMove();

        return move;
    }*/

    @Override
    public int getMove(Node currentGameNode) {
        return 0;
    }

    @Override
    public String getName() {
        return "Human";
    }
}
