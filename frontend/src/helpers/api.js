import axios from 'axios'

let mode = 'localhost';

if (mode == 'localhost') {
    axios.defaults.baseURL = 'http://localhost:8080/';
}
else if (mode == 'heroku') {
    axios.defaults.baseURL;
}

import qs from 'qs'

export function post(_this, url, payload, successCallback, errorCallback, headers = '') {

    return axios({
        method: 'POST',
        url: url,
        data: payload,
        headers: headers
    }).then(response => {
        successCallback( response );
    }).catch(error => {
        if(!error.status)
            console.log('network error');
        console.log(error.response);
        if(errorCallback)
            errorCallback( error );
    });
}

export function get(_this, url, payload, successCallback, errorCallback) {
    let headers = '';

    return axios({
        method: 'GET',
        url: url,
        params: payload.params,
        headers: headers,
        paramsSerializer: params => {
            return qs.stringify(params, { arrayFormat: "repeat" })
        },
    }).then(response => {
        successCallback( response );
    }).catch(error => {
        if(errorCallback)
            errorCallback( error );
    });
}


export function del(_this, url, payload, successCallback, errorCallback) {
    let headers = '';

    return axios({
        method: 'DELETE',
        url: url,
        headers: headers,
        payload: payload
    }).then(response => {
        successCallback( response );
    }).catch(error => {
        if(errorCallback)
            errorCallback( error );
    });


}

export function download(_this, url, payload, successCallback, errorCallback) {

    axios({
        method: 'get',
        url: url,
        responseType: 'arraybuffer'
    })
        .then(response => {

            forceFileDownload(response);
            successCallback(response);

        })
        .catch(error => {
            if(errorCallback) {
                errorCallback(error);
                console.log(error);
            }
        });
}

function forceFileDownload(response) {

    let pattern = /"(.*?[^\\])"/;
    let contentDisposition = response.headers['content-disposition'];
    let match = contentDisposition.match(pattern);
    let filename = match[1];

    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename) //or any other extension
    document.body.appendChild(link)
    link.click()
}