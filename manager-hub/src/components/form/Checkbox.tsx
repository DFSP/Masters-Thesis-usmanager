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

import React from "react";
import styles from "./Checkbox.module.css";
import {capitalize} from "../../utils/text";

interface Props {
    id: string;
    label?: string;
    checked: boolean;
    disabled?: boolean;
    onCheck: (id: string, checked: boolean) => void;
}

interface State {
    checked: boolean,
}

export class Checkbox extends React.Component<Props, State> {

    state: State = {
        checked: this.props.checked,
    };

    private handleCheckbox = (event: React.ChangeEvent<HTMLInputElement>): void => {
        const {checked} = event.target;
        this.props.onCheck(this.props.id, checked);
        this.setState({checked: checked});
    };

    public render() {
        const {id, label, disabled} = this.props;
        const {checked} = this.state;
        return (
            <div className={styles.checkbox}>
                <label>
                    <input id={id}
                           type="checkbox"
                           disabled={disabled}
                           onChange={this.handleCheckbox}
                           checked={checked}/>
                    <span id={'checkbox'} className={styles.formCheckbox}>
                        {capitalize(label || id)}
                    </span>
                </label>
            </div>

        )
    }

}
