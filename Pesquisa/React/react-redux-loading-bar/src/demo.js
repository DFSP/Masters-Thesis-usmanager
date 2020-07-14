import React from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { bindActionCreators } from 'redux'

import { fetchPhotos } from './reducers/photos'
import { calculatePi } from './reducers/pi'

import Header from './components/header'
import Photos from './components/photos'
import Pi from './components/pi'

class Demo extends React.Component {
  constructor(props) {
    super(props)

    this.boundHandleFetchPhotos = this.handleFetchPhotos.bind(this)
    this.boundHandleCalculatePi = this.handleCalculatePi.bind(this)
  }

  handleFetchPhotos() {
    this.props.actions.fetchPhotos()
  }

  handleCalculatePi() {
    this.props.actions.calculatePi()
  }

  render() {
    return (
      <div className="center">
        <Header />
        <main className="p3 mx-auto">
          <Photos
            photos={this.props.photos}
            handleFetchPhotos={this.boundHandleFetchPhotos}
          />
          <Pi
            pi={this.props.pi}
            handleCalculatePi={this.boundHandleCalculatePi}
          />
        </main>
      </div>
    )
  }
}

Demo.propTypes = {
  actions: PropTypes.object.isRequired,
  photos: PropTypes.array.isRequired,
  pi: PropTypes.number.isRequired,
}

const mapStateToProps = state => ({
  photos: state.photos,
  pi: state.pi.default || 0,
})

const mapDispatchToProps = dispatch => ({
  actions: bindActionCreators(
    { fetchPhotos, calculatePi },
    dispatch,
  ),
})

export default connect(mapStateToProps, mapDispatchToProps)(Demo)
