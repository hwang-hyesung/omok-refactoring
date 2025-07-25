import * as board from './board.js';
import { startGame, sendStone, currentTurn} from './game.js';
import {myRole} from "../matching/matching";

window.onload = () => {
    const gameId = sessionStorage.getItem('gameId') || 'defaultGame';
    startGame(gameId);

    board.initBoardEvents(sendStone, currentTurn, myRole);
};
