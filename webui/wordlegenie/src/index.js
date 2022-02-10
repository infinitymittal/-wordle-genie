import React from 'react'
import ReactDOM from 'react-dom'
import './index.css'

const WORD_LENGTH = 5
const MAX_ATTEMPTS = 6

class CharButton extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			value:0
		}
	}
	
	handleClick() {
		this.setState({value:(this.state.value+1)%3})
	}
	
	render() {
		return (
			<button className="char-button" 
				onClick={()=>this.handleClick()}
				style={{background:CharButton.backGroundColors[this.state.value], 
					color:CharButton.textColors[this.state.value]}}>
			  {this.props.ch}
			</button>
		)
	}
}
CharButton.backGroundColors = ['black', 'yellow', 'Lime']
CharButton.textColors = ['white', 'black', 'black']

class AttemptRow extends React.Component {
	constructor(props) {
		super(props)
	}

	render() {
		const charButtons = [...Array(WORD_LENGTH).keys()].map(i=> 
			<CharButton key = {i} ch={this.props.move.word[i].toUpperCase()}/>
		)
		return (
		<div className="attempt-row">
			{charButtons}
			<button className="next-button" onClick={()=>this.props.onNextClick(this.props.rowId)}>
			  NEXT
			</button>			
		</div>
		)
	}
}

class MainLayout extends React.Component {
	constructor(props) {
		super(props)
		console.log(props)
		this.state = {
			colors: Array(MAX_ATTEMPTS).fill(Array(WORD_LENGTH).fill(0)),
			moves: [...Array(MAX_ATTEMPTS).keys()].map(i=>MainLayout.rootMove)
		}
	}
	
	handleNextClick(mainLayout, rowId) {
		console.log(rowId)
		const bucket = 0
		const newMoves = [...this.state.moves]
		newMoves[rowId+1] = newMoves[rowId].bucketToMove[bucket]
		this.setState({moves:newMoves})
	}

	render() {
		return ([...Array(MAX_ATTEMPTS).keys()].map(i=>
				<AttemptRow key={i} rowId={i} 
				move={this.state.moves[i]}
				onNextClick={rowId=>this.handleNextClick(this, rowId)}
		/>))
	}
}

MainLayout.rootMove = require("./bestresult.json")
console.log(MainLayout.rootMove.word)

class Instructions extends React.Component {
	render() {
		return (
		<div style={{ textAlign: "center", color: "black"}} >
			This is a Wordle Solver for ALL words.
			<br/>WIP. Not ready yet!!!!
			<ol type="1">
				<li>Enter the word given here in Wordle.</li>
				<li>Change color of the alphabets below by tapping.</li>
				<li>Match the colors to those given by Wordle.</li>
				<li>Press NEXT to get the next word.</li>
				<li>Repeat from Step 1 for next word.</li>
			</ol>
			You can start again by pressing RESET."
		</div>
		)
	}
}

// ========================================
ReactDOM.render(
  <React.StrictMode>
	<div>
		<Instructions />
		<MainLayout/>
	</div>
  </React.StrictMode>,
  document.getElementById('root')
)