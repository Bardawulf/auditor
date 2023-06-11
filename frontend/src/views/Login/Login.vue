<template>
  <div class="login-container">
    <h1 class="login-title">Login</h1>
    <form @submit.prevent="handleLogin" class="login-form">

      <div class="form-group">
        <label for="username" class="form-label">Username:</label>
        <input type="text" id="username" v-model="username" class="form-input" required>
      </div>
      <div class="form-group">
        <label for="password" class="form-label">Password:</label>
        <input type="password" id="password" v-model="password" class="form-input" required>
      </div>
      <button type="submit" class="login-button">Login</button>
    </form>
  </div>
</template>


<script>
import { mapActions, mapGetters } from 'vuex';
import {post} from "../../helpers/api";

export default {
  data() {
    return {
      username: '',
      password: '',
    };
  },
  methods: {
    // Use the mapActions helper to access the login action from Vuex
    ...mapActions(['login']),
    async handleLogin() {
      // Check if the user is already authenticated
      if (this.isAuthenticated) {
        this.$router.push('/');
        return;
      }

      let data = {
        username: this.username,
        password: this.password,
      };

      post(
          this,
          '/authenticate',
          data,
          async response => {
            if (response.data.token != null) {
              await this.login(data);
              if (this.isAuthenticated) {
                localStorage.setItem("token", response.data.token)
                this.$router.push('/');
              } else {
                alert('Authentication failed');
              }
            } else {
              alert('Login failed');
            }
          },
          error => {
            // Handle the API request error
            console.error(error);
          }
      );

    },
  },
  computed: {
    // Access the isAuthenticated getter from Vuex
    ...mapGetters(['isAuthenticated']),
  },
};
</script>


<style scoped>
.login-container {
  max-width: 400px;
  margin: 0 auto;
  padding: 40px;
  background-color: #f3f3f3;
  border-radius: 4px;
}

.login-title {
  text-align: center;
  margin-bottom: 20px;
}

.login-form {
  display: flex;
  flex-direction: column;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  font-weight: bold;
}

.form-input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  margin-left: 32px;
}

.login-button {
  padding: 10px;
  background-color: #4caf50;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.login-button:hover {
  background-color: #45a049;
}

@media (max-width: 480px) {
  .login-container {
    padding: 20px;
  }
}
</style>
  