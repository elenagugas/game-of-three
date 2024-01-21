# Just Eat Takeaway: Game of Three - Coding Challenge

## How it works
Each player starts his own application. Available endpoints:

* Set game mode
  * /game/set-auto
  * /game/set-manual
* Start the game
  * /game/start/{number}
  * /game/start/auto
* In manual mode trigger this endpoint to make your move
  * /game/make-my-move