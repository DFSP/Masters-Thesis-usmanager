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
import M, {DatepickerOptions} from "materialize-css";

interface Props {
    className?: string;
    id: string;
    name: string;
    value: string;
    disabled?: boolean;
    onSelect: (date?: string) => void;
    onChange: (e: React.FormEvent<HTMLInputElement>) => void;
    options?: Partial<DatepickerOptions>;
}

interface State {
    selectedDate?: string;
}

export class Datepicker extends React.Component<Props, State> {

    state: State = {
        selectedDate: '',
    };
    private datepicker = createRef<HTMLInputElement>();

    public componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>, snapshot?: any): void {
        if (prevState.selectedDate === this.state.selectedDate) {
            this.initDatepicker();
        }
        M.updateTextFields();
    }

    public render() {
        const {className, id, name, value, disabled, onChange} = this.props;
        return (
            <input
                className={`datepicker ${className}`}
                type='text'
                id={id}
                name={name}
                value={value || ''}
                disabled={disabled}
                autoComplete='off'
                onSelect={onChange}
                ref={this.datepicker}/>
        );
    }


    private clear = () => {
        this.setState({selectedDate: undefined});
    }

    private initDatepicker = (): void => {
        const options: Partial<DatepickerOptions> = {
            i18n: {
                cancel: 'Cancelar',
                done: 'Confirmar',
                clear: 'Apagar',
                months: ['Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho', 'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'],
                nextMonth: 'Próximo Mês',
                previousMonth: 'Mês Anterior',
                monthsShort: ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'],
                weekdays: ['Domingo', 'Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado'],
                weekdaysShort: ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'],
                weekdaysAbbrev: ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'],
            },
            format: 'dd/mm/yyyy',
            minDate: new Date(),
            defaultDate: new Date(this.props.value),
            /*showClearBtn: true,*/
            autoClose: true,
            onSelect: this.onSelect,
            onClose: this.onClose,
            ...this.props.options
        }
        M.Datepicker.init(this.datepicker.current as Element, options)
    };

    private onClose = () =>
        this.props.onSelect(this.state.selectedDate);

    private onSelect = (selectedDate: Date): void =>
        this.setState({selectedDate: selectedDate.toLocaleDateString('pt')});

}