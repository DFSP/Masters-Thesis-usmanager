import React, {createRef} from "react";
import M, { DatepickerOptions } from "materialize-css";

interface Props {
  className?: string;
  id: string;
  name: string;
  value: string;
  disabled?: boolean;
  onSelect: (date: string) => void;
  onChange: (e: React.FormEvent<HTMLInputElement>) => void;
  options?: Partial<DatepickerOptions>;
}

interface State {
  selectedDate: string;
}

export class Datepicker extends React.Component<Props, State> {

  private datepicker = createRef<HTMLInputElement>();

  state: State = {
    selectedDate: '',
  };

  private initDatepicker = (): void => {
    M.Datepicker.init(this.datepicker.current as Element, {
      format: 'dd/mm/yyyy',
      minDate: new Date(),
      defaultDate: new Date(this.props.value),
      showClearBtn: true,
      onSelect: this.onSelect,
      onClose: this.onClose,
      ...this.props.options
    });
  };

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    if (prevState.selectedDate === this.state.selectedDate) {
      this.initDatepicker();
    }
    M.updateTextFields();
  }

  private onClose = () =>
    this.props.onSelect(this.state.selectedDate);

  private onSelect = (selectedDate: Date): void =>
    this.setState({ selectedDate: selectedDate.toLocaleDateString('pt') });

  public render() {
    const {className, id, name, value, disabled, onChange} = this.props;
    return (
      <input
        className={`datepicker ${className}`}
        type="text"
        id={id}
        name={name}
        value={value || ''}
        disabled={disabled}
        autoComplete="off"
        onChange={onChange}
        ref={this.datepicker}/>
    );
  }

}