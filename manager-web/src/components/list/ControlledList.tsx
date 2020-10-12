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
import M from "materialize-css";
import List from "./List";
import styles from "./ControlledList.module.css";
import PerfectScrollbar from "react-perfect-scrollbar";
import ScrollBar from "react-perfect-scrollbar";
import BaseComponent from "../BaseComponent";
import {deleteData} from "../../utils/api";
import {decodeHTML} from "../../utils/text";
import InputDialog from "../dialogs/InputDialog";
import {IFormModal, IValues, RestOperation} from "../form/Form";

interface IDropdown {
    id: string,
    title: string,
    empty: string,
    data: string[],
    onSelect?: (selected: any) => void,
    formModal?: IFormModal
}

interface ControlledListProps<T> {
    dataKey: string[],
    isLoading?: boolean;
    error?: string | null;
    emptyMessage?: string;
    data?: T[];
    dropdown?: IDropdown;
    formModal?: IFormModal;
    show: (index: number, element: T, separate: boolean, checked: boolean,
           handleCheckbox: (event: React.ChangeEvent<HTMLInputElement>) => void) => JSX.Element;
    onAdd?: (data: string) => void;
    onAddInput?: (input: IValues) => void;
    onRemove?: (data: string[]) => void;
    onDelete?: RestOperation;
    removeButtonText?: string;
    entitySaved?: boolean;
    sort?: (a: T, b: T) => number
}

type Props<T> = ControlledListProps<T>;

type DataState<T> = { value: T, isChecked: boolean, isNew: boolean } | undefined;

interface State<T> {
    [key: string]: DataState<T>;
}

export default class ControlledList<T> extends BaseComponent<Props<T>, State<T>> {

    static defaultProps = {
        dataKey: []
    }
    state: State<T> = {};
    private globalCheckbox = createRef<HTMLInputElement>();
    private dropdown = createRef<HTMLButtonElement>();
    private scrollbar: (ScrollBar | null) = null;
    private selected?: string;

    public componentDidMount(): void {
        this.initDropdown();
    }

    public componentDidUpdate(prevProps: Readonly<Props<T>>, prevState: Readonly<State<T>>, snapshot?: any): void {
        if (this.globalCheckbox.current) {
            this.globalCheckbox.current.checked = Object.values(this.state)
                .map(data => !data || data.isChecked)
                .every(checked => checked);
        }
        if (prevProps.data !== this.props.data) {
            const previousData = prevProps.data?.map(this.getDataStateKey) || [];
            this.invalidateStateData(previousData);
            this.setState((this.props.data || []).reduce((state: State<T>, data) => {
                const dataStateKey = this.getDataStateKey(data);
                state[dataStateKey] = {value: data, isChecked: false, isNew: false};
                return state;
            }, {}));
        }
        if (prevProps.entitySaved !== this.props.entitySaved) {
            this.setState(Object.values(this.state).reduce((state: State<T>, data) => {
                if (data) {
                    const dataStateKey = this.getDataStateKey(data.value);
                    state[dataStateKey] = {value: data.value, isChecked: false, isNew: false};
                }
                return state;
            }, {}));
        }
        this.initDropdown();
    }

    public render() {
        const {isLoading, error, emptyMessage, dropdown, formModal, sort, removeButtonText} = this.props;
        // @ts-ignore
        let data: T[] = Object.values(this.state).filter(data => data).map(data => data.value);
        if (sort) {
            data = data.sort(sort);
        }
        const DataList = List<T>();
        return (
            <div>
                <div className='controlsContainer'>
                    {!error && data.length > 0 && (
                        <p className={`${styles.nolabelCheckbox}`}>
                            <label>
                                <input type="checkbox"
                                       onChange={this.handleGlobalCheckbox}
                                       ref={this.globalCheckbox}/>
                                <span/>
                            </label>
                        </p>
                    )}
                    {!error && dropdown && (
                        <>
                            <button
                                className={`dropdown-trigger btn-floating btn-flat btn-small waves-effect waves-light right tooltipped ${styles.formButton}`}
                                data-position="bottom" data-tooltip={dropdown.title}
                                data-target={`dropdown-${dropdown.id}`}
                                ref={this.dropdown}>
                                <i className="material-icons">add</i>
                            </button>
                            <ul id={`dropdown-${dropdown.id}`}
                                className={`dropdown-content ${styles.dropdown}`}>
                                <li className={`${styles.disabled}`}>
                                    <a className={`${!dropdown?.data.length ? 'dropdown-empty' : ''}`}>
                                        {dropdown.data.length ? dropdown.title : dropdown.empty}
                                    </a>
                                </li>
                                <PerfectScrollbar ref={(ref) => {
                                    this.scrollbar = ref;
                                }}>
                                    {dropdown.data.map((data, index) =>
                                        <li key={index} onClick={!dropdown?.formModal ? this.onAdd : this.setSelected}>
                                            <a className={'modal-trigger'} data-target={dropdown.formModal?.id}>
                                                {data}
                                            </a>
                                        </li>
                                    )}
                                </PerfectScrollbar>
                            </ul>
                            {dropdown?.formModal && this.inputDialog(dropdown?.formModal, true)}
                        </>
                    )}
                    {(!error && formModal &&
                        <>
                            <button
                                className={`modal-trigger btn-floating btn-flat btn-small waves-effect waves-light right tooltipped ${styles.button}`}
                                data-position="bottom" data-tooltip={formModal.title}
                                data-target={formModal.id}>
                                <i className="material-icons">add</i>
                            </button>
                            {this.inputDialog(formModal)}
                        </>
                    )}
                    <button className={`btn-flat btn-small waves-effect waves-light red-text right ${styles.button}`}
                            style={!error && Object.values(this.state)
                                .map(item => item?.isChecked || false)
                                .some(checked => checked)
                                ? {transform: "scale(1)"}
                                : {transform: "scale(0)"}}
                            onClick={this.onRemove}>
                        {removeButtonText || 'Remove'}
                    </button>
                </div>
                <DataList
                    isLoading={isLoading}
                    error={error}
                    emptyMessage={emptyMessage}
                    list={data}
                    show={this.show}/>
            </div>
        )
    }

    private initDropdown = () =>
        M.Dropdown.init(this.dropdown.current as Element,
            {
                onOpenEnd: this.onOpenDropdown
            });

    private onOpenDropdown = () =>
        this.scrollbar?.updateScroll();

    private handleGlobalCheckbox = (event: React.ChangeEvent<HTMLInputElement>) => {
        const {checked} = event.target;
        this.setState(state =>
            Object.entries(state).filter(([_, data]) => data).reduce((newState: State<T>, [data, dataState]) => {
                if (dataState) {
                    newState[data] = {value: dataState.value, isChecked: checked, isNew: dataState.isNew};
                }
                return newState;
            }, {}));
    };

    private handleCheckbox = (event: React.ChangeEvent<HTMLInputElement>) => {
        const {id: data, checked} = event.target;
        if (this.state[data]) {
            this.setState(state => ({
                [data]: {
                    // @ts-ignore
                    value: state[data].value,
                    isChecked: checked,
                    // @ts-ignore
                    isNew: state[data].isNew
                }
            }));
        }
    };

    private getDataStateKey = (data: T): string => {
        if (typeof data === 'string') {
            return data;
        }
        const key = this.props.dataKey;
        // @ts-ignore
        let stateKey = data[key[0]];
        for (let i = 1; i < key.length; i++) {
            // @ts-ignore
            let currentKey = key[i];
            if (!stateKey[currentKey]) {
                return stateKey;
            }
            stateKey = stateKey[currentKey];
        }
        return stateKey;
    };

    private getDataState = (data: T): DataState<T> => {
        const dataKey = this.getDataStateKey(data);
        return this.state[dataKey];
    };

    private show = (data: T, index: number): JSX.Element => {
        const separate = index !== Object.entries(this.state).filter(([_, data]) => data).length - 1;
        const checked = this.getDataState(data)?.isChecked || false;
        return this.props.show(index, data, separate, checked, this.handleCheckbox)
    };

    private onAdd = (event: React.MouseEvent<HTMLLIElement>): void => {
        const data = decodeHTML((event.target as HTMLLIElement).innerHTML);
        // @ts-ignore
        this.setState({[data]: {value: data, isChecked: false, isNew: true}});
        this.props.onAdd?.(data);
    };

    private onAddDropdownModalInput = (input: any): void => {
        if (this.selected) {
            let selectedProperty;
            for (let i = this.props.dataKey.length - 1; i >= 0; i--) {
                selectedProperty = {[this.props.dataKey[i]]: selectedProperty ? selectedProperty : this.selected};
            }
            input = {...selectedProperty, ...input};
            const key = this.getDataStateKey(input);
            this.setState({[key]: {value: input, isChecked: false, isNew: true}});
            this.props.onAddInput?.(input);
        }
    };

    private onAddFormModalInput = (input: any): void => {
        const key = this.getDataStateKey(input);
        if (Object.keys(this.state).includes(key)) {
            return super.toast(`Unable to add ${key}`, 10000, 'already exists');
        }
        this.setState({[key]: {value: input, isChecked: false, isNew: true}});
        this.props.onAddInput?.(input);
    };

    private onRemove = (): void => {
        if (!this.props.onDelete) {
            return console.error('onDelete not defined');
        }
        const checkedData = Object.entries(this.state).filter(([_, data]) => data?.isChecked);
        const unpersistedData = checkedData.filter(([_, data]) => data?.isNew).map(([name, _]) => name);
        if (unpersistedData.length) {
            this.invalidateStateData(unpersistedData);
            this.props.onRemove?.(unpersistedData);
        }
        const persistedData = checkedData.filter(([_, data]) => !data?.isNew).map(([name, _]) => name);
        if (persistedData.length) {
            const {url} = this.props.onDelete;
            if (persistedData.length === 1) {
                deleteData(`${url}/${persistedData[0]}`, () => this.onDeleteSuccess(persistedData), this.onDeleteFailure);
            } else {
                deleteData(url, () => this.onDeleteSuccess(persistedData), this.onDeleteFailure, persistedData);
            }
        }
    };

    private onDeleteSuccess = (data: string[]): void => {
        this.invalidateStateData(data);
        this.props.onDelete?.successCallback(data);
    };

    private invalidateStateData = (data: string[]): void =>
        this.setState(data.reduce((state: State<T>, data: string) => {
            state[data] = undefined;
            return state;
        }, {}));

    private onDeleteFailure = (reason: string, args: any): void =>
        this.props.onDelete?.failureCallback(reason, args);

    private inputDialog = (formModal: IFormModal, preSelected?: boolean): JSX.Element => {
        return <InputDialog id={formModal.id}
                            title={formModal.title}
                            fields={formModal.fields}
                            values={formModal.values}
                            position={formModal.position}
                            confirmCallback={preSelected ? this.onAddDropdownModalInput : this.onAddFormModalInput}
                            fullscreen={formModal.fullScreen}
                            scrollbar={formModal.scrollbar}>
            {formModal?.content()}
        </InputDialog>;
    };

    private setSelected = (event: any) => {
        this.selected = decodeHTML((event.target as HTMLLIElement).innerHTML);
        this.props.dropdown?.onSelect?.(this.selected);
    };

}