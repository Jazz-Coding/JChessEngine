# JChessEngine

A chess engine written in Java. The engine finds the best move through a deep search of the chess game tree, applying intelligent pruning techniques (alpha-beta pruning, killer heuristic, aspiration windows, among other common optimizations), and memoization (via a transposition table) to achieve higher depths and reach competitive playing strength. Bundled with a Swing GUI for playing against it, at a depth of 7 plies the engine has a playing strength of ~2300 ELO.

The engine also exposes many of its internal settings that can be freely played around with to see the effects on performance and gameplay.


![java_H6WW7rM1Wh](https://github.com/Jazz-Coding/JChessEngine/assets/52354702/24edc2c4-f846-46f7-af3b-9cda244dbdd0)

## Setting Breakdown
<details>
  <summary>Depth Limit</summary>
  <br>
  
  The maximum depth through the game tree the engine will search before stopping calculation, effectively how far the engine can "look ahead" into the future. A deeper search results in stronger play, at the cost of performance.
  
This setting also behaves differently if "Quiescence Search" is enabled, as that feature enables the engine to briefly exceed the depth limit in certain circumstances.
</details>

<details>
  <summary>Quiescence Search</summary>
  <br>
  If enabled, the engine, upon reaching its maximum depth (also known as the _horizon_), will continue searching deeper but with a limited scope - specifically only non-_quiet_ moves will be considered; these are moves like captures and checks, and are far less numerous than the set of all moves in a position. By searching only these moves, tactical capture-exchange sequences and checks can be fully explored until the position has "quieted down" as it were. This significantly improves playing strength as the engine is no longer blind to "obvious" errors in its play, like leaving its queen hanging at its horizon, and only has marginal performance impact due to the scarcity of non-quiet moves.
</details>

<details>
  <summary>Alpha Beta Pruning</summary>
  <br>
If enabled, the most substantial optimization of the engine takes effect.
 
Naively, the engine finds best moves by considering all possible moves, then considering all possible replies to each of those moves, then considering all replies to those replies, etc... The engine explores the resulting game-tree depth-first, and at each level makes the assumption that the opponent considers all moves and picks whichever one either maximizes the score (for white) or minimizes the score (for black, who is considered to strive for negative scores), alternating as the tree is ascended. Then at the root, all moves will have been considered and the optimal move will be found. 

This process (known as minimax) is extremely slow, but it turns out if we just remember what each player is "assured" at any given point (their other options that they have already fully explored due to the depth-first nature), we can make logical statements that allow us to skip nodes of the tree we can be sure our opponent will not proceed to. These assured scores are known as alpha (the maximizer's least assured score), and beta (the minimizer's greatest assured score) respectively, hence the term "alpha-beta" pruning. 

These scores can be thought of as the worst case scenario for the players, and all we need is to find one "refutation" move from _us_ to "refute" the move from our opponent that brought us here to stop searching. Suppose there are 35 replies from us (the average chess branching factor) after our opponent's move, and our first one turns out to be even worse than the opponent's assured score, we can stop searching the other 34 moves (resulting in enormous savings since each move may be an entire sub-tree), since our opponent isn't going to let us have the option of playing this refutation move. When this happens it is known as a _beta-cutoff_.

Recursively, each player can use the same logic to reason about the other player's moves.
Whether such a reduction actually occurs in practice depends on the move ordering (which of our options we try first), if we can guess intelligently which move is likely to be good (for instance a move that was really good in a similar position), and try it out first, we maximize the chances that, if a refutation exists, we find it quickly and can stop searching. 

In the code, we can make use of the mathematical fact: min(A,B) = -max(-B,-A); this makes the maximizer and minimizer's function calls truly identical, just with negated parameters in the call. When this is done the system is called a "_negamax_" framework.
</details>

<details>
  <summary>Killer Heuristic</summary>
  <br>
With alpha-beta pruning enabled, it's important we try moves likely to be very good to begin with. A commonly used heuristic to increase the chances of this is known as the "killer heuristic". The idea is that we maintain a list of "killer moves" that, at this level on the tree, resulted in beta-cutoffs. Usually only a few of these are recorded, and enabling this setting will enable the "Number of Killers" box to be edited, with a default of 2 killers remembered at each depth (that are constantly replaced as new ones are found).

<details>
  <summary>Number of Killers</summary>
  <br>
The number of killer moves to remember at each level of the tree during alpha-beta pruning. Generally only a few are required, since they change quite frequently, and with too many remembered the ones at the beginning of the list are unlikely to be used again as opposed to more recently discovered ones.
</details>

</details>

<details>
  <summary>Transposition Table</summary>
  <br>
In chess, the same position can re-occur through different series of moves, or we can reach a position we already calculated from several moves in the past. Either way, we can employ the dynamic programming technique of memoization by creating a table of the positions we've analyzed to some extent in the past; if these reoccur (tested through a special hash table), we can save having to perform recursive calls, or at least narrow the scope of the recursive calls, significantly improving performance. This can also be used in conjunction with iterative deepening to significantly improve search performance.

  <details>
  <summary>Maximum Size</summary>
  <br>
The maximum number of entries in the transposition table. By default, this setting is set to 100,000,000. Pressing "max" will compute this automatically from available RAM (so you can allocate more through the JVM to increase it beyond the initial limit). Past this limit, new positions will overwrite older ones in the table.
</details>
</details>
    
<details>
  <summary>Iterative Deepening</summary>
  <br>
With this setting enabled, instead of immediately going to depth 8, the engine goes to depth 3, then depth 4, then depth 5... all the way to depth 8. This sounds pointless on the surface, but searches at shallower depths actually provide us a considerable amount of information. If a search at depth 5 revealed a move was very good, there is a good chance at depth 6 it will also be good. Had we started at depth 6, we could not know this move was good unless we fully explore the tree to depth 6, i.e. we may be able to establish the quality of this move a whole ply earlier on the game tree. If we remember these moves, iterative deepening actually improves engine performance considerably.

</details>

<details>
  <summary>Piece Values</summary>
  <br>
The value the engine assigns to each piece. This will make it value different pieces differently. For instance you could adjust the settings to make the engine prefer bishops over knights, or vice versa, and it would then bring its full strength to the table achieving that preference, resulting in some interesting gameplay. Piece values can be negative, which will result in the engine trying to lose those pieces as soon as possible (useful for anti-chess). 
</details>


<details>
  <summary>Value Piece Positions</summary>
  <br>
With this enabled, defers to a table of ideal positions for each piece (pawns in the centre, king on the safe corners, etc). Very useful for making the engine play human moves in the opening at low depths (where it otherwise would see no problem with playing g4 for example!).
</details>
