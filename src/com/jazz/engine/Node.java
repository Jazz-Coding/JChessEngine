package com.jazz.engine;

import com.jazz.engine.data.Move;
import com.jazz.engine.gui.Statistics;
import com.jazz.engine.search.evaluation.Evaluator;
import com.jazz.engine.data.BBs;
import com.jazz.engine.moves.MoveGenerator;
import com.jazz.engine.moves.MoveUpdater;
import com.jazz.engine.utils.datastructures.HistogramHashMap;
import com.jazz.mvc.utils.enums.MoveResult;
import com.jazz.old.Board;

import java.util.List;

import static com.jazz.engine.ChessEngine.BLACK;
import static com.jazz.engine.ChessEngine.WHITE;
import static com.jazz.mvc.utils.enums.MoveResult.*;

/**
 * A node in the chess game tree.
 * Corresponds to a position.
 */
public class Node {
    private BBs bitboards;
    private HistogramHashMap<Long> boardHistory = new HistogramHashMap<>();

    public Node(BBs bitboards) {
        this.bitboards = bitboards;
    }
    public List<Integer> childMoves() throws ArrayIndexOutOfBoundsException{
        return MoveGenerator.allMoves(bitboards);
    }

    public void makeMove(int move){

        MoveUpdater.makeMove(bitboards, move);
        long newHash = bitboards.hash;
        boardHistory.increment(newHash);


    }

    public void unmakeMove(int move){
        long oldHash = bitboards.hash;
        MoveUpdater.unmakeMove(bitboards, move);
        boardHistory.decrement(oldHash);
    }

    public boolean isTRF(){
        return boardHistory.occurrences(hash()) >= 3;
    }

    public float material(float[] pieceValues, int color){
        return Evaluator.material(bitboards.pieceBBs, pieceValues,color);
    }

    private boolean isLateGame = false;
    public void checkIsLateGame(float[] pieceValues){
        if(material(pieceValues,WHITE) <= 13 && material(pieceValues,BLACK) <= 13){
            isLateGame = true;
        }else {
            isLateGame = false;
        }
    }

    public float heuristicEvaluation(float[] pieceValues, boolean doPositionEval){
        return Evaluator.heuristicEvaluation(bitboards.pieceBBs,bitboards.color, doPositionEval, isLateGame, pieceValues);
    }

    public long hash(){
        return bitboards.hash;
    }

    public int color(){
        return bitboards.color;
    }

    public int[] getCastlingRights(){
        return bitboards.castlingRights;
    }

    public boolean isGameEnding(){
        return childMoves().isEmpty() || isTRF();
    }

    public MoveResult getResult(){
        if(isTRF()){
            return STALEMATE_TFR;
        } // TODO: Fifty move rule.

        if(childMoves().isEmpty()){
            if(kingInCheck()){
                if(color()==WHITE)  return CHECKMATE_BLACK_WINS;
                else                return CHECKMATE_WHITE_WINS;
            } else {
                return STALEMATE_NORMAL;
            }
        }

        return SUCCESS;
    }

    public boolean isMoveLoud(int childMove){
        if(Move.isCapture(childMove) || Move.isPromotion(childMove)) return true;
        makeMove(childMove); // Otherwise play the move and see if it checks our opponent.
        boolean comesWithCheck = kingInCheck();
        unmakeMove(childMove);
        return comesWithCheck;
    }

    public boolean isMoveIllegal(int childMove){
        return MoveGenerator.illegalMove(bitboards,childMove,color());
    }

    public boolean kingInCheck(){
        return MoveGenerator.isKingInCheck(bitboards,color());
    }

    public static Node from(Board board){
        return new Node(BBs.from(board));
    }

    public static Node duplicate(Node existing){
        return new Node(BBs.duplicate(existing.bitboards));
    }

    public Board toBoard(){
        return bitboards.toBoard();
    }

    public BBs getBitboards() {
        return bitboards;
    }

    public void clearEnPassants(){
        bitboards.enPassantable[WHITE] = 0L;
        bitboards.enPassantable[BLACK] = 0L;
    }

    public void restoreEnPassants(long[] previousCopy){
        bitboards.enPassantable[WHITE] = previousCopy[WHITE];
        bitboards.enPassantable[BLACK] = previousCopy[BLACK];
    }

    public long[] getEnPassants(){
        return new long[]{bitboards.enPassantable[WHITE],bitboards.enPassantable[BLACK]};
    }

    public void computePositionalScores(Statistics statistics) {
        Evaluator.updatePositionalScoresUI(bitboards.pieceBBs, statistics,isLateGame);
    }
}
