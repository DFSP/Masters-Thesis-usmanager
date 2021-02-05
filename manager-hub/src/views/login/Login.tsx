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

import React, {createRef} from 'react'
import {basicAuthenticate, isAuthenticated, registerSuccessfulLogin} from "../../utils/auth";
import {RouteComponentProps} from "react-router";
import styles from './Login.module.css';
import M from "materialize-css";
import {bindActionCreators} from "redux";
import {showSidenavByUser} from "../../actions";
import {connect} from "react-redux";
import {AxiosError} from "axios";
import BaseComponent from "../../components/BaseComponent";

interface State {
    username: string;
    password: string;
    showPassword: boolean;
}

interface DispatchToProps {
    showSidenavByUser: (value: boolean) => void;
}

type Props = DispatchToProps & RouteComponentProps;

class Login extends BaseComponent<Props, State> {

    state = {
        username: '',
        password: '',
        showPassword: false,
    };
    private tabs = createRef<HTMLUListElement>();

    public componentDidMount(): void {
        M.Tabs.init(this.tabs.current as Element);
        M.updateTextFields();
    }

    public render() {
        if (isAuthenticated()) {
            this.props.history.push(`/gestor`);
        }
        const {username, password, showPassword} = this.state;
        return (
            <div className={`container ${styles.container} row`}>
                <ul className={`tabs ${styles.tabs} col s9 m6 l6 offset-s1 offset-m3 offset-l3`} ref={this.tabs}>
                    <li className={`tab col s12`}><a className={styles.title}>Introduza as credenciais</a></li>
                </ul>
                <div className={`tab-content ${styles.tabContent} col s9 m6 l6 offset-s1 offset-m3 offset-l3`}>
                    <form onSubmit={this.handleLogin}>
                        <div className="input-field col s12">
                            <i className="material-icons prefix">account_circle</i>
                            <label className="active" htmlFor="username">Username</label>
                            <input id="username" name="username" value={username}
                                   type="text" required onChange={this.handleChange}/>
                        </div>
                        <div className="input-field col s12">
                            <i className="material-icons prefix">vpn_key</i>
                            <label className="active" htmlFor="password">Password</label>
                            <input id="password" name="password" value={password}
                                   autoComplete="off"
                                   type={showPassword ? "text" : "password"}
                                   required
                                   onChange={this.handleChange}/>
                            <i className="material-icons suffix" onClick={this.handleShowPassword}>
                                {showPassword ? "visibility_off" : "visibility"}
                            </i>
                        </div>
                        <button className="btn btn-flat login-button right slide"
                                type="submit" tabIndex={0} onClick={this.handleLogin}>
                            Entrar
                        </button>
                    </form>
                </div>
            </div>
        );
    }

    private handleChange = ({target: {name, value}}: React.ChangeEvent<HTMLInputElement>) =>
        this.setState({[name]: value} as Pick<State, any>);

    private handleShowPassword = () =>
        this.setState({showPassword: !this.state.showPassword});

    private handleLogin = (e: React.FormEvent) => {
        e.preventDefault();
        const {username, password} = this.state;
        basicAuthenticate(username, password)
            .then(() => {
                registerSuccessfulLogin(username, password);
                this.props.history.push(`/gestor`);
            }).catch((e: AxiosError) => {
            super.toast(`Autenticação falhou`, 7500, e.response?.status === 401 ? 'Username e/ou password inválido' : e.message, true, true);
        })
    };

}

const mapDispatchToProps = (dispatch: any): DispatchToProps =>
    bindActionCreators({showSidenavByUser}, dispatch);

export default connect(null, mapDispatchToProps)(Login);
