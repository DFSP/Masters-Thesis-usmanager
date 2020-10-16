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