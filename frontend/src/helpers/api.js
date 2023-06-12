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
    const token = localStorage.getItem('token');
    const authorizationHeader = { Authorization: `Bearer ${token}` };

    let parsedHeaders = {};
    try {
        parsedHeaders = JSON.parse(headers);
    } catch (error) {
        console.error('Error parsing headers:', error);
    }

    const mergedHeaders = { ...parsedHeaders, ...authorizationHeader };

    return axios({
        method: 'POST',
        url: url,
        data: payload,
        headers: mergedHeaders
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
    const token = localStorage.getItem('token');
    const headers = {
        Authorization: `Bearer ${token}`,
    };

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
    const token = localStorage.getItem('token');
    const headers = {
        Authorization: `Bearer ${token}`,
    };

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
    const token = localStorage.getItem('token');
    const headers = {
        Authorization: `Bearer ${token}`,
    };

    axios({
        method: 'get',
        url: url,
        headers: headers,
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