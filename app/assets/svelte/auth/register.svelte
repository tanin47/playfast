<script lang="ts">
import Button from '../common/_button.svelte'
import {FetchError, invokeOnEnter, post} from "../common/form/index";
import ErrorPanel from '../common/form/_error_panel.svelte';

let isLoading = false
let errors: string[] = []
let form = {
  email: '',
  password: ''
}

async function submit(): Promise<void> {
  isLoading = true
  try {
    const _json = await post('/register', form)

    window.location.href = '/'
  } catch (e) {
    isLoading = false
    errors = (e as FetchError).messages
  }
}
</script>

<div class="hero bg-base-200 min-h-screen">
  <div class="hero-content flex-col justify-center items-center">
    <div class="card bg-base-100 min-w-[400px] w-full max-w-sm shrink-0 shadow-2xl">
      <div class="card-body flex flex-col gap-4" onkeydown={invokeOnEnter(submit)}>
        <h1 class="card-title">Register</h1>
        <span class="label">Email</span>
        <input type="email" class="input w-full" placeholder="Email" data-test-id="email" bind:value={form.email}/>
        <span class="label">Password</span>
        <input type="password" class="input w-full" placeholder="Password" data-test-id="password"
               bind:value={form.password}/>
        <div class="text-xs text-neutral-400 leading-relaxed">
          By signing up, you agree to
          our <a href="/terms-of-service" target="_blank" class="link">Terms Of Service</a>
          and <a href="/privacy-policy" target="_blank" class="link">Privacy Policy</a>.
        </div>
        <ErrorPanel {errors}/>
        <Button {isLoading} onClick={submit} dataTestId="submit-button">Register</Button>
        <div>
          Already have an account? Please
          <a href="/login" class="link link-primary">Login</a>
        </div>
      </div>
    </div>
  </div>
</div>


<style lang="scss">
</style>
