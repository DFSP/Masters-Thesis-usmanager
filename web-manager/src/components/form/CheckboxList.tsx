import React from "react";
import ListItem from "../list/ListItem";
import listItemStyles from "../../components/list/ListItem.module.css";
import styles from "./CheckboxList.module.css";
import {camelCaseToSentenceCase} from "../../utils/text";

interface Props {
  id: string;
  name: string;
  values: any[];
  disabled?: boolean;
  onCheck: (listId: string, itemId: string, checked: boolean) => void;
}

interface State {
  values: { value: string, checked: boolean }[],
}

export class CheckboxList extends React.Component<Props, State> {

  private getCheckboxValues = () =>
    this.props.values.map(v => ({ value: v, checked: false }));

  state: State = {
    values: [],
  };

  public componentDidMount(): void {
    this.setState({ values: this.getCheckboxValues() });
  }

  public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
    if (prevProps.values !== this.props.values) {
      this.setState({ values: this.getCheckboxValues() });
    }
  }

  private handleCheckbox = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const {id, checked} = event.target;
    const stateValue = this.state.values.find(value => value.value === id);
    if (stateValue) {
      stateValue.checked = checked;
      this.props.onCheck(this.props.id, id, checked);
    }
    this.setState({ values: this.state.values });
  };

  private item = (index: number, region: string, checked: boolean): JSX.Element => {
    return (
      <ListItem key={index}>
        <div className={`${listItemStyles.nonListContent}`}>
          <label>
            <input id={region}
                   type="checkbox"
                   onChange={this.handleCheckbox}
                   checked={checked}/>
            <span id={'checkbox'}>
                 {region}
            </span>
          </label>
        </div>
      </ListItem>
    );
  };

  public render() {
    const {id, name} = this.props;
    const {values} = this.state;
    return (
      <div id={id} className='noMargin'>
        <h6 className={`white-text ${styles.title}`}>{camelCaseToSentenceCase(name)}</h6>
        {values.map((value, index) =>
          this.item(index, value.value, value.checked)
        )}
      </div>
    )
  }

}
