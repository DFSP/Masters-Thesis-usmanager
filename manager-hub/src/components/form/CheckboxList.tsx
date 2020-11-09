/*
 * MIT License
 *
 * Copyright (c) 2020 manager
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import React, {createRef} from "react";
import ListItem from "../list/ListItem";
import listItemStyles from "../../components/list/ListItem.module.css";
import styles from "./CheckboxList.module.css";
import {camelCaseToSentenceCase} from "../../utils/text";
import Empty from "../list/Empty";

interface Props {
    id: string;
    name: string;
    values: any[];
    disabled?: boolean;
    required: boolean;
    onCheckList: (listId: string, itemId: string, checked: boolean) => void;
}

interface State {
    values: { value: string, checked: boolean }[],
}

export class CheckboxList extends React.Component<Props, State> {

    private globalCheckbox = createRef<HTMLInputElement>();

    state: State = {
        values: [],
    };

    public componentDidMount(): void {
        this.setState({values: this.getCheckboxValues()});
    }

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (prevProps.values !== this.props.values) {
            this.setState({values: this.getCheckboxValues()});
        }
        if (this.globalCheckbox.current) {
            this.globalCheckbox.current.checked = Object.values(this.state.values).every(data => data.checked);
        }
    }

    private handleGlobalCheckbox = (event: React.ChangeEvent<HTMLInputElement>) => {
        const {checked} = event.target;
        this.state.values.forEach(value => {
            if (checked !== value.checked) {
                this.props.onCheckList(this.props.id, value.value, checked);
            }
        });
        this.setState({values: Object.values(this.state.values).map(v => ({value: v.value, checked: checked}))});
    }

    public render() {
        const {id, name, disabled, required} = this.props;
        const {values} = this.state;
        return (
            <div id={id} className='noMargin'>
                <h6 className={`white-text ${styles.title} ${values.length ? 'left' : ''}`}>
                    {camelCaseToSentenceCase(name)}
                    {required && <span className={`red-text ${styles.requiredLabel}`}>*</span>}
                </h6>
                {values.length
                    ? <p className={`${styles.globalCheckbox}`}>
                        <label>
                            <input type="checkbox"
                                   disabled={disabled}
                                   onChange={this.handleGlobalCheckbox}
                                   ref={this.globalCheckbox}/>
                            <span/>
                        </label>
                    </p>
                    : <Empty message={`Sem ${name} para selecionar`}/>}
                {values.map((value, index) =>
                    this.item(index, value.value, value.checked)
                )}
            </div>
        )
    }

    private getCheckboxValues = () =>
        this.props.values.map(v => ({value: v, checked: false}));

    private handleCheckbox = (event: React.ChangeEvent<HTMLInputElement>): void => {
        const {id, checked} = event.target;
        const stateValue = this.state.values.find(value => value.value === id);
        if (stateValue) {
            stateValue.checked = checked;
            this.props.onCheckList(this.props.id, id, checked);
        }
        this.setState({values: this.state.values});
    };

    private item = (index: number, option: string, checked: boolean): JSX.Element => {
        return (
            <ListItem key={index}>
                <div className={`${listItemStyles.nonListContent}`}>
                    <label>
                        <input id={option}
                               type="checkbox"
                               onChange={this.handleCheckbox}
                               checked={checked}/>
                        <span id={'checkbox'}>
                            {option}
                        </span>
                    </label>
                </div>
            </ListItem>
        );
    };

}
