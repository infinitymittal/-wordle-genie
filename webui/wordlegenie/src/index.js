import React from 'react'
import ReactDOM from 'react-dom'
import './index.css'
import {toast} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

toast.configure()

const WORD_LENGTH = 5
const MAX_ATTEMPTS = 6
const ALL_GREEN = 242

class CharButton extends React.Component {
	render() {
		return (
			<button className="char-button" 
				onClick={this.props.onCharClick}
				style={{background:CharButton.backGroundColors[this.props.color], 
				color:CharButton.textColors[this.props.color]}}>
			  {this.props.ch}
			</button>
		)
	}
}

CharButton.backGroundColors = ['black', 'yellow', 'Lime']
CharButton.textColors = ['white', 'black', 'black']

class AttemptRow extends React.Component {
	render() {
		if(this.props.isHidden)
			return null
		const charButtons = [...Array(WORD_LENGTH).keys()].map(i=> 
			<CharButton key = {i}
				ch={this.props.move.word[i].toUpperCase()}
				color={this.props.colors[i]}
				onCharClick={() =>this.props.onCharClick(this.props.rowId, i)}
			/>
		)
		return (
		<div className="attempt-row">
			{charButtons}
			<button className="next-button" onClick={()=>this.props.onNextClick(this.props.rowId)}>
			  NEXT🡺
			</button>			
		</div>
		)
	}
}

class ResetButton extends React.Component {
	render() {
		return (<div className="reset-container">
					<button className="reset-button" onClick={()=>this.props.onResetClick()}>
						RESET ⭮
					</button>	
				</div>
				)
	}
}

class MainLayout extends React.Component {
	constructor(props) {
		super(props)
		this.state = this.getStartState()
	}
	
	getStartState() {
		return {
			rowColors: [...Array(MAX_ATTEMPTS).keys()].map(i=>Array(WORD_LENGTH).fill(0)),
			moves: [...Array(MAX_ATTEMPTS).keys()].map(i=>MainLayout.rootMove),
			isRowHidden: [...Array(MAX_ATTEMPTS).keys()].map(i=> i>0)
		}
	}
	
	handleNextClick(rowId) {
		let bucket = 0
		this.state.rowColors[rowId].forEach(color=> {
			bucket=bucket*3
			bucket+=color
		})
		if(rowId>=MAX_ATTEMPTS-1 || bucket==242) {
			toast.success("That's all Folks!", {position: toast.POSITION.TOP_CENTER}, {autoClose:3000})
			return
		}
		const newMoves = this.state.moves
		if(!(newMoves[rowId].bucketToMove.hasOwnProperty(bucket))) {
			toast.error("Your argument is invalid. Check the colors.", {position: toast.POSITION.TOP_CENTER}, {autoClose:3000})
			return
		}
		newMoves[rowId+1] = newMoves[rowId].bucketToMove[bucket]

		const newIsRowHidden = this.state.isRowHidden
		newIsRowHidden[rowId+1] = false
		this.setState({moves:newMoves, isRowHidden:newIsRowHidden})
	}

	handleCharClick(rowId, charId) {
		const newRowColors = this.state.rowColors
		newRowColors[rowId][charId] = (newRowColors[rowId][charId]+1)%3
		this.setState({rowColors:newRowColors})
	}
	
	handleReset() {
		this.setState(this.getStartState())
	}

	render() {
		return (<div>{
					[...Array(MAX_ATTEMPTS).keys()].map(i=>
					<AttemptRow key={i} rowId={i} 
						move={this.state.moves[i]}
						colors={this.state.rowColors[i]}
						onNextClick={(rowId)=>this.handleNextClick(rowId)}
						onCharClick={(rowId,charId) =>this.handleCharClick(rowId, charId)}
						isHidden={this.state.isRowHidden[i]}
					/>)
				}
				<ResetButton onResetClick={()=>this.handleReset()}/>
				</div>)
	}
}

MainLayout.rootMove = require("./bestresult.json")

class Instructions extends React.Component {
	render() {
		return (
		<div className="instructions">
			<h1>WORDLE GENIE</h1>
			This is a Wordle Solver for ALL words.
			<ol type="1">
				<li>Enter the word given here in Wordle.</li>
				<li>Tap alphabets to change their color.</li>
				<li>Match the colors result by Wordle.</li>
				<li>Press NEXT to get the next word.</li>
				<li>Repeat from Step 1 for next word.</li>
			</ol>
			You can start again by pressing RESET.
		</div>
		)
	}
}

// ========================================
ReactDOM.render(
  <React.StrictMode>
	<div>
		<Instructions/>
		<MainLayout/>
	</div>
  </React.StrictMode>,
  document.getElementById('root')
)