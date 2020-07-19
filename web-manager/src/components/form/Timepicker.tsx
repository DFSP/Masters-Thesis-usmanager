import React, {createRef} from "react";
import M, {TimepickerOptions} from "materialize-css";
import {zeroPad} from "../../utils/text";

interface Props {
  className?: string;
  id: string;
  name: string;
  value: string;
  disabled?: boolean;
  onSelect: (time: string) => void;
  onChange: (e: React.FormEvent<HTMLInputElement>) => void;
  options?: Partial<TimepickerOptions>;
}

interface State {
  selectedTime: string;
}

export class Timepicker extends React.Component<Props, State> {

  state: State = {
    selectedTime: '',
  };
  private timepicker = createRef<HTMLInputElement>();

  public componentDidMount(): void {
    this.initTimepicker();
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    if (prevState.selectedTime === this.state.selectedTime) {
      this.initTimepicker();
    }
    M.updateTextFields();
  }

  public render() {
    const {className, id, name, value, disabled, onChange} = this.props;
    return (
      <input className={`timepicker ${className}`}
             type="text"
             id={id}
             name={name}
             value={value || ''}
             disabled={disabled}
             autoComplete="off"
             onChange={onChange}
             ref={this.timepicker}/>
    )
  }

  private initTimepicker = (): void => {
    M.Timepicker.init(this.timepicker.current as Element, {
      twelveHour: false,
      defaultTime: this.props.value,
      showClearBtn: true,
      onSelect: this.onSelect,
      onCloseEnd: this.onClose,
      ...this.props.options
    });
  };

  private onClose = () =>
    this.props.onSelect(this.state.selectedTime);

  private onSelect = (hour: number, minute: number): void =>
    this.setState({selectedTime: String(`${zeroPad(hour, 2)}:${zeroPad(minute, 2)}`)});

}