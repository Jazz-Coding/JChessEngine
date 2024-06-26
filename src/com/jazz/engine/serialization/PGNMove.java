package com.jazz.engine.serialization;

import com.jazz.engine.data.Move;
import com.jazz.engine.data.Pieces;

import static com.jazz.engine.ChessEngine.*;
import static com.jazz.engine.utils.bits.BBUtils.ordinalToSqr;

public class PGNMove {
    private String piece=""; //e.g. Q (queen)

    private String originFile=""; // e.g. h (h file)
    private String originRank=""; // e.g. 4 (4th rank)

    private String action=""; // e.g. x (captures)

    private String destinationFile="";
    private String destinationRank="";

    private String endTag=""; // e.g. +(check) #(checkmate) =Q (promotion to queen)

    private String specialOverride = ""; // for castles;
    private boolean castles = false;

    public PGNMove(String piece, String originFile, String originRank, String action, String destinationFile, String destinationRank, String endTag) {
        this.piece = piece;
        this.originFile = originFile;
        this.originRank = originRank;
        this.action = action;
        this.destinationFile = destinationFile;
        this.destinationRank = destinationRank;
        this.endTag = endTag;
    }

    public PGNMove(int move) {
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int opt = Move.getOriginPieceType(move);
        int dpt = Move.getDestinationPieceType(move);
        boolean isCaptures = Move.isCapture(move);
        boolean isPromotion = Move.isPromotion(move);
        boolean isCastle = Move.isCastles(move);

        if(isCastle){
            setCastles(true);
            int castleType = Move.castleType(move);
            if(castleType==KINGSIDE) {
                setSpecialOverride("O-O");
            } else {
                setSpecialOverride("O-O-O");
            }
            return;
        }

        setOriginFile(getSquareFile(ordinalToSqr(from)));
        setOriginRank(getSquareRank(ordinalToSqr(from)));

        setDestinationFile(getSquareFile(ordinalToSqr(to)));
        setDestinationRank(getSquareRank(ordinalToSqr(to)));
        switch (opt){
            case Pieces.pawn -> setPiece("");
            case Pieces.knight -> setPiece("N");
            case Pieces.bishop -> setPiece("B");
            case Pieces.rook -> setPiece("R");
            case Pieces.queen -> setPiece("Q");
            case Pieces.king -> setPiece("K");
        }

        if(isCaptures){
            setAction("x");
        }

        if(isPromotion){
            int promotionType = Move.getPromotionType(move);
            switch (promotionType){
                case Pieces.knight -> setEndTag("=N");
                case Pieces.bishop -> setEndTag("=B");
                case Pieces.rook -> setEndTag("=R");
                case Pieces.queen -> setEndTag("=Q");
            }
        }
    }

    public void setPiece(String piece) {
        this.piece = piece;
    }

    public void setOriginFile(String originFile) {
        this.originFile = originFile;
    }

    public void setOriginRank(String originRank) {
        this.originRank = originRank;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setDestinationFile(String destinationFile) {
        this.destinationFile = destinationFile;
    }

    public void setDestinationRank(String destinationRank) {
        this.destinationRank = destinationRank;
    }

    public void setEndTag(String endTag) {
        this.endTag = endTag;
    }

    public void setSpecialOverride(String specialOverride) {
        this.specialOverride = specialOverride;
    }

    public void setCastles(boolean castles) {
        this.castles = castles;
    }
    private static String getSquareFile(long square){
        int ordinal = Long.numberOfTrailingZeros(square);
        int file = ordinal % 8;

        return String.valueOf((char) (file + 97));
    }
    private static String getSquareRank(long square){
        int ordinal = Long.numberOfTrailingZeros(square);
        int rank = (ordinal / 8)+1;

        return String.valueOf(rank);
    }

    @Override
    public String toString() {
        if(castles){
            return specialOverride;
        } else {
            return piece + originFile + originRank + action + destinationFile + destinationRank + endTag;
        }
    }
}
