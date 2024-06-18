# Contents
- [Summary](#summary)
- [Game Rules](#game-rules)
- [Minimax Implementation](#minimax-implementation)
- [Gallery](#gallery)
- [See Also](#see-also)

# Summary
This is a pure Java implementation of Quoridor, a strategy game designed by Mirko Marchesi where the goal is to block your opponent's pawn and get your pawn to the other side. It was developed for the ICS4U1 culminating assignment with Mr. Skuja at A. Y. Jackson Secondary School. The implementation of the hard difficulty agent was the most difficult and interesting part of this project. For more details, check out the [Minimax Implementation](#minimax_implementation) section of this README.

## Features
- Command line interface
- Player versus Player
- Player versus Agent
- Multiple agent difficulties (normal, hard)
- Save game functionality
- Load game functionality

# Game Rules
## Game Components
- 1 Game Board (9x9 grid of spaces)
- 20 Fences
- 2 Pawns

## Objective
The objective of Quoridor is to be the first player to move your pawn to the opposite side of the board.

## Setup
1. Place the game board between the players.
2. Each player starts with a it in the center space of their starting edge (the middle space on their side of the board).
3. Each player gets 10 fences.

## Gameplay
### Players and Turns
- The game is played 2 players.
- Players alternate taking turns.
- On their turn, a player must either move their pawn or place a fence.

### Moving the Pawn
- A pawn can be moved one space in any of the four orthogonal directions (forward, backward, left, right).
- If a pawn is adjacent to another pawn, and there are no fences blocking the path, the player can jump over the adjacent pawn.
- If there is a fence behind the adjacent pawn, the player can move to either side of the adjacent pawn.
- If both side spaces are also blocked by fences, the player can only jump back to their original position.

### Placing a Fence
- A player can place a fence between two sets of two spaces. Fences must be placed to divide four squares into two and two.
- Fences can be placed horizontally or vertically.
- A fence cannot be placed in a way that completely blocks a pawn from reaching the opposite side of the board. Each pawn must always have at least one valid path to the other side.

### Winning the Game
- The first player to move their pawn to any space on the opposite side of the board wins the game.

## Additional Rules
- A player cannot skip their turn.
- Once a fence is placed, it cannot be moved.
- Players cannot move diagonally.

# Minimax Implementation
I would not have been able to implement the minimax algorithm without Sebastian Lague's ([See Here](#see-also)) wonderfully consise and easily digestible video on minimax trees and alpha-beta pruning. I followed his implementation for the most part. The evaluation function for is implemented as the minimizing pawn's distance-to-goal subtracted by the maximizing pawn's distance-to-goal.

## Move Caching
In this program, there are three different caches:

- the optimal move for each position (the return value of the top minimax function) is cached
- the static evaluation for each evaluated position is cached
- the children of each position is cached

All three of these caches are serialized onto the disk at the end of the program as .ser files. When the program starts, the serialized files are then loaded into the memory. While this does increase the loading time of the program to around 30 seconds, it also **significantly** improves the performance of the minimax tree.

## Heuristic Ordering
Moves are ordered heuristically in two ways. I started by initializing the row of squares that the agent is attempting to block to 0 and then running dijkstra's to get the distances from these squares to all other squares.\
\
I then used a priority queue to order the squares by the distances. The best 16 squares that walls can be placed on are first evaluated, followed by the pawn moves, followed by the remaining walls.\
\
This number, 16, is completely arbitrary, but it arises as 1/4 of all squares walls can be placed on, since walls occupy two rows/columns and (9-1)^2 is 64.\
\
I settled on this ordering because is very performant, halving the previous heuristic ordering iteration's time-to-move.

## Depth
Unfortunately, the minimax algorithm takes too long (>30s) to evaluate a move past depths of around 3-4, so I ended up settling at a search depth of 3 for usability reasons. This depth goes up to 4 or 5 when the pawns near the ends of the board.\
\
This version at depth 3 still remarkably better and faster than the first iterations of the algorithm at depth 2 due to heuristic ordering improvements, move caching, and alpha-beta pruning as mentioned above.

# Gallery
![image](https://github.com/aicheye/Quoridor/assets/55955758/164ccdd1-816e-4cd3-b896-a3e085186b91)
![image](https://github.com/aicheye/Quoridor/assets/55955758/6b987950-8808-4d67-9ef9-f8d736228b02)
![image](https://github.com/aicheye/Quoridor/assets/55955758/37e4e521-a3d5-43a0-81f1-32216eeb3449)
![image](https://github.com/aicheye/Quoridor/assets/55955758/dab837aa-2312-4467-89d7-62d42379e097)

# See Also
- https://en.wikipedia.org/wiki/Quoridor
- https://en.gigamic.com/modern-classics/107-quoridor.html
- https://www.youtube.com/watch?v=l-hh51ncgDI&t=432s&ab_channel=SebastianLague
