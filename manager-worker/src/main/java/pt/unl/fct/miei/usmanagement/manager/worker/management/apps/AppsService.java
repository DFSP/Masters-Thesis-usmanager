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

package pt.unl.fct.miei.usmanagement.manager.worker.management.apps;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.unl.fct.miei.usmanagement.manager.database.apps.AppEntity;
import pt.unl.fct.miei.usmanagement.manager.database.apps.AppRepository;
import pt.unl.fct.miei.usmanagement.manager.database.apps.AppServiceEntity;
import pt.unl.fct.miei.usmanagement.manager.worker.exceptions.EntityNotFoundException;

@Slf4j
@Service
public class AppsService {

	private final AppRepository apps;

	public AppsService(AppRepository apps) {
		this.apps = apps;
	}

	public List<AppEntity> getApps() {
		return apps.findAll();
	}

	public AppEntity getApp(Long id) {
		return apps.findById(id).orElseThrow(() ->
			new EntityNotFoundException(AppEntity.class, "id", id.toString()));
	}

	public AppEntity getApp(String appName) {
		return apps.findByNameIgnoreCase(appName).orElseThrow(() ->
			new EntityNotFoundException(AppEntity.class, "name", appName));
	}

	public List<AppServiceEntity> getServices(String appName) {
		assertAppExists(appName);
		return apps.getServices(appName);
	}

	private void assertAppExists(String appName) {
		if (!apps.hasApp(appName)) {
			throw new EntityNotFoundException(AppEntity.class, "name", appName);
		}
	}

}
