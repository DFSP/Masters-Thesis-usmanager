import React from "react";

interface Props {
  icon: string;
  tooltip: {
    text?: string,
    deactivatedText?: string,
    activatedText?: string,
    position: 'left' | 'right' | 'bottom' | 'top'
  };
  clickCallback: () => void;
  active?: boolean;
  automatic?: boolean;
  offset?: number;
}

interface State {
  isActive: boolean;
}

export default class ActionButton extends React.Component<Props, State> {

  state: State = {
    isActive: (this.props.automatic && this.props.active) || false
  };

  handleOnClick = () =>  {
    if (this.props.automatic) {
      this.setState(state => ({isActive: !state.isActive}));
    }
    this.props.clickCallback();
  };

  public render() {
    const {icon, offset, tooltip, automatic} = this.props;
    const {isActive} = this.state;
    return (
      <div className={`fixed-action-btn tooltipped waves-effect btn-floating grey darken-${isActive ? '4' : '3'}`}
           style={offset ? {right: `${offset * 55 + 23}px`} : undefined}
           data-position={tooltip.position}
           data-tooltip={`${automatic ? (isActive ? tooltip.activatedText : tooltip.deactivatedText) : tooltip.text}`}
           onClick={this.handleOnClick}>
        <i className="large material-icons">{icon}</i>
      </div>
    );
  }

}