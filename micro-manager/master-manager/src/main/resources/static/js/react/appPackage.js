/*
 * MIT License
 *
 * Copyright (c) 2020 micro-manager
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

let $ = require('jquery');
let React = require('react');
let Component = React.Component;
import Utils from './utils';
import {MainLayout} from './globalComponents';
import {ServiceCard} from './service';

class AppPackageCard extends Component {

    constructor(props) {
        super(props);
        let propAppPackage = this.props.appPackage;
        let defaultEdit =  propAppPackage.id === 0;
        this.state = { isLaunchActive: false, appPackage: propAppPackage, data: [], availableRegions: [],
            regionSelected: '', countrySelected:'', citySelected:'', loading: false, isEdit: defaultEdit};
    }

    componentDidMount() {
        this.loadAppServices();
    };

    componentDidUpdate() {
        M.updateTextFields();
        M.Collapsible.init(document.querySelectorAll('.collapsible'));
        if (this.state.isLaunchActive) {
            let elems = this.region;
            M.FormSelect.init(elems);
        }
    };

    loadRegions = () => {
        this.setState({loading: true});
        let self = this;
        let url = '/regions';
        Utils.ajaxGet(url, function(data) {
            self.setState({availableRegions: data, loading: false});
        });
    };

    loadAppServices = () => {
        this.setState({loading: true});
        let self = this;
        let url = '/apps/' + this.state.appPackage.id + '/services';
        Utils.ajaxGet(url, function(data) {
            self.setState({data: data, loading: false});
        });
    };

    handleChangeRegion = (event) => {
        this.setState({regionSelected: event.target.value});
    };

    handleChangeCountry = (event) => {
        this.setState({countrySelected: event.target.value});
    };

    handleChangeCity = (event) => {
        this.setState({citySelected: event.target.value});
    };

    renderRegionsSelect = () => {
        let regions;
        if (this.state.availableRegions) {
            regions = this.state.availableRegions.map(function (region) {
                return (
                    <option key={region.regionName} value={region.regionName}>{region.regionName + ' (' + region.regionDescription + ')'}</option>
                );
            });
            return regions;
        }
    };

    renderRegionsSelectTotal = () => {
        return (
            <div className="input-field col s6">
                <select ref={ region => this.region = region} defaultValue="" name="region" id="region" onChange={this.handleChangeRegion} >
                    <option value="" disabled="disabled">Choose region</option>
                    {this.renderRegionsSelect()}
                </select>
                <label htmlFor="Region">Region</label>
            </div>
        )
    };

    renderNormal = () => {
        return (
            <div>
                <h5>App name</h5>
                <div>{this.state.appPackage.appName}</div>
            </div>
        );
    };

    onClickLaunch = () => {
        this.loadRegions();
        this.setState({isLaunchActive: true});
    };

    onClickCancelLaunch = () => {
        let instance = M.FormSelect.getInstance(this.region);
        instance.destroy();
        this.setState({isLaunchActive: false});
    };

    renderLaunchApp = () => {
        let style = {marginLeft: "10px"};
        let cancelButton =
            <a title="Cancel" style={style} className="btn-floating waves-effect waves-light red darken-4" onClick={this.onClickCancelLaunch}>
                <i className="material-icons">clear</i>
            </a>;
        if (this.state.isLaunchActive) {
            return (
                <div>
                    <div className="row">
                        {this.renderRegionsSelectTotal()}
                        <div className="input-field col s6">
                            <input onChange={this.handleChangeCountry} defaultValue={this.state.countrySelected} name='country' id='country' type="text" autoComplete="off"/>
                            <label htmlFor="country">Country</label>
                        </div>
                    </div>
                    <div className="row">
                        <div className="input-field col s6">
                            <input onChange={this.handleChangeCity} defaultValue={this.state.citySelected} name='city' id='city' type="text" autoComplete="off"/>
                            <label htmlFor="city">City</label>
                        </div>
                        <div className="input-field col s6">
                            <a title="Launch App" style={style} className="btn-floating waves-effect waves-light" onClick={this.onLaunchApp}>
                                <i className="material-icons">send</i>
                            </a>
                            {cancelButton}
                        </div>
                    </div>
                </div>
            );
        }
        else {
            return (
                <div className="row">
                    <div className="col s12">
                        <button disabled={this.state.appPackage.id === 0} style={style} className="waves-effect waves-light btn-small" onClick={this.onClickLaunch}>
                            Launch
                        </button>
                    </div>
                </div>
            );
        }
    };

    onClickEdit = () => {
        let setEdit = !this.state.isEdit;
        if (!setEdit && this.state.appPackage.id === 0) {
            this.props.updateNewApp(true);
        }
        this.setState({isEdit: setEdit});
    };

    onClickRemove = () => {
        let formAction = '/apps/' + this.state.appPackage.id;
        let self = this;
        Utils.formSubmit(formAction, 'DELETE', {}, function (data) {
            M.toast({html: "<div>App deleted successfully!</div>"});
            self.props.onRemove();
        });
    };

    onLaunchApp = () => {
        let formAction = '/containers/app/' + this.state.appPackage.id;
        let self = this;
        let dataToSend = JSON.stringify({
            region: self.state.regionSelected,
            country: self.state.countrySelected,
            city: self.state.citySelected
        });
        Utils.formSubmit(formAction, 'POST', dataToSend, function (data) {
            console.log(data);
            M.toast({
                html: "<div>App launched successfully!</div>",
                options: {displayLength: 10000}
            });
            self.onClickCancelLaunch();
        });
    };

    handleChange = (event) => {
        let name = event.target.name;
        let newData = this.state.appPackage;
        newData[name] = event.target.value;
        this.setState({appPackage: newData});
    };

    onSubmitForm = (e) => {
        e.preventDefault();
        let self = this;
        let formId = this.state.appPackage.id + 'appForm';
        let formAction = '/apps/' + this.state.appPackage.id;
        let dataToSend = Utils.convertFormToJson(formId);
        Utils.formSubmit(formAction, 'POST', dataToSend, function (data) {
            let newData = self.state.appPackage;
            let oldId = newData.id;
            newData.id = data;
            self.setState({appPackage: newData, isEdit: false});
            if (oldId === 0) {
                self.props.updateNewApp(false);
            }
            M.toast({html: "<div>App saved successfully!</div>"});
        });
    };

    renderForm = () => {
        return (
            <form id={this.state.appPackage.id + 'appForm'} onSubmit={this.onSubmitForm}>
                <div className="input-field">
                    <input value={this.state.appPackage.appName} onChange={this.handleChange} placeholder='App Name' name='appName' id={this.state.appPackage.id + 'appName'} type="text" autoComplete="off"/>
                    <label htmlFor={this.state.appPackage.id + 'hostname'}>App Name</label>
                </div>
                <button className="btn waves-effect waves-light" type="submit" name="action">
                    Save
                    <i className="material-icons right">send</i>
                </button>
            </form>
        );
    };

    render() {
        let appServices;
        if (this.state.data) {
            appServices = this.state.data.map(function (appService) {
                return (
                    <li key={appService.service.id} >
                        <div className="collapsible-header">{appService.service.serviceName}</div>
                        <div className="collapsible-body">
                            <ServiceCard renderSimple={true} service={appService.service}/>
                        </div>
                    </li>
                );
            });
        }
        let app = this.state.isEdit ? this.renderForm() : this.renderNormal();
        let style = {marginLeft: '5px'};
        return (
            <div id={'app' + this.props.index} className='row'>
                <div className='col s12'>
                    <div className='card'>
                        <div className='card-content'>
                            <div className="right-align">
                                {this.renderLaunchApp()}
                                <a style={style} className="waves-effect waves-light btn-small" onClick={this.onClickEdit}>{this.state.isEdit ? 'Cancel' : 'Edit'}</a>
                                <button disabled={this.state.appPackage.id === 0} style={style} className="waves-effect waves-light btn-small red darken-4" onClick={this.onClickRemove}>Remove</button>
                            </div>
                            {app}
                            <h5>Services</h5>
                            <ul className="collapsible">
                                {appServices}
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export class AppPackages extends Component {

    constructor(props) {
        super(props);
        this.state = { data: [], loading: false, showAdd: true };
    }

    componentDidMount() {
        this.loadApps();
        let instances = M.Tooltip.init(document.querySelectorAll('.tooltipped'));
        this.setState({tooltipInstances: instances});
    }

    componentDidUpdate() {
        if(this.state.data.length > 0) {
            var lastIndex = this.state.data.length - 1;
            if(this.state.data[lastIndex].id === 0) {
                var offset = $('#app' + lastIndex).offset().top;
                $(window).scrollTop(offset);
                this.state.tooltipInstances[0].destroy();
            }
        }
    }

    loadApps = () => {
        this.setState({ loading: true });
        let self = this;
        Utils.ajaxGet('/apps',
            function (data) {
                self.setState({data: data, loading: false});
            });
    };

    updateNewApp = (isCancel) => {
        let newData = this.state.data;
        if (isCancel) {
            newData.splice(newData.length - 1, 1);
            this.setState({data: newData, showAdd: true});
        }
        else {
            this.setState({showAdd: true});
        }
        let instances = M.Tooltip.init(document.querySelectorAll('.tooltipped'));
        this.setState({tooltipInstances: instances});
    };

    addApp = () => {
        let newApp = {
            id: 0, appName: ''
        };
        let newData = this.state.data;
        newData.push(newApp);
        this.setState({data: newData, showAdd: false});
    };

    componentWillUnmount() {
        this.state.tooltipInstances[0].destroy();
    }

    render() {
        let appPackagesNodes;
        let self = this;
        if (this.state.data) {
            appPackagesNodes = this.state.data.map(function (appPackage, index) {
                return (
                    <AppPackageCard key={appPackage.id} index={index} appPackage={appPackage} onRemove={self.loadApps} updateNewApp={self.updateNewApp}/>
                );
            });
        }
        return (
            <MainLayout title='App packages'>
                {appPackagesNodes}
                <div className="fixed-action-btn tooltipped" data-position="left" data-tooltip="Add app package">
                    <button disabled={!this.state.showAdd} className="waves-effect waves-light btn-floating btn-large grey darken-4" onClick={this.addApp}>
                        <i className="large material-icons">add</i>
                    </button>
                </div>
            </MainLayout>
        );
    }
}