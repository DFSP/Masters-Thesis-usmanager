import React from "react";
import M from "materialize-css";

interface TextBoxProps<T> {
  className: string;
  id: string;
  name: string;
  value?: any;
  disabled?: boolean;
  valueToString?: (v: T) => string
  onChange: (e: React.FormEvent<HTMLInputElement>) => void;
  onBlur: (e: React.FormEvent<HTMLInputElement>) => void;
  hidden?: boolean;
}

interface State {
  show: boolean;
}

export class TextBox<T> extends React.Component<TextBoxProps<T>, State> {

  state = {
    show: this.props.hidden === undefined || !this.props.hidden,
  };

  public componentDidUpdate(prevProps: Readonly<TextBoxProps<T>>, prevState: Readonly<any>, snapshot?: any): void {
    M.updateTextFields();
  }

  render(): any {
    const {className, id, name, value, disabled, hidden, valueToString, onChange, onBlur} = this.props;
    const {show} = this.state;
    return (
      <>
        <input
          className={className}
          id={id}
          name={name}
          type={show ? "text" : "password"}
          value={typeof value === 'object' && valueToString ? valueToString(value) : (value || '')}
          disabled={disabled}
          autoComplete="off"
          onChange={onChange}
          onBlur={onBlur}
          formNoValidate={true}/>
        {hidden !== undefined && <i className="material-icons suffix" onClick={this.handleToggleVisibility}>
          {show ? "visibility" : "visibility_off"}
        </i>}
      </>
    )
  }

  private handleToggleVisibility = () =>
    this.setState({show: !this.state.show});

}
