import Vue from 'vue';
import Vuex from 'vuex';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    barColor: 'rgba(0, 0, 0, .8), rgba(0, 0, 0, .8)',
    barImage: '',
    drawer: null,
    snackbars: [],
    isAuthenticated: false,
  },
  mutations: {
    SET_BAR_IMAGE(state, payload) {
      state.barImage = payload;
    },
    SET_DRAWER(state, payload) {
      state.drawer = payload;
    },
    SET_SNACKBAR(state, snackbar) {
      state.snackbars = state.snackbars.concat(snackbar);
    },
    SET_AUTHENTICATED(state, isAuthenticated) {
      state.isAuthenticated = isAuthenticated;
    },
  },
  actions: {
    setSnackbar({ commit }, snackbar) {
      snackbar.showing = true;
      snackbar.color = snackbar.color || 'success';
      commit('SET_SNACKBAR', snackbar);
    },
    login({ commit }) {
      // Perform login logic, e.g., making an API call and checking credentials
      // If login is successful, set isAuthenticated to true
      commit('SET_AUTHENTICATED', true);
    },
    logout({ commit }) {
      // Perform logout logic, e.g., clearing session data, tokens, etc.
      // Set isAuthenticated to false
      commit('SET_AUTHENTICATED', false);
    },
  },
  getters: {
    isAuthenticated: state => state.isAuthenticated,
  },
});
