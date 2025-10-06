<script lang="ts">
import {FetchError, post} from "../common/form/index";
import {PreferredLangs, type User} from "../common/models";

export let user: User

let isLoading: {[key: string]: boolean} = {}

async function update(field: keyof User, value: any): Promise<void> {
  isLoading[field] = true
  try {
    await post('/update', {
      [field]: value ?? 'SET_TO_NULL'
    })


  } catch (e) {
    console.log((e as FetchError).messages)
  } finally {
    isLoading[field] = false
  }
}
</script>

<div class="hero bg-base-200 min-h-screen">
  <div class="hero-content flex-col justify-center items-center">
    <div class="card bg-base-100 min-w-[400px] w-full max-w-sm shrink-0 shadow-2xl">
      <div class="card-body flex flex-col gap-6">
        <div class="flex gap-4 items-center">
          <span class="label">Preferred Language</span>
          <select
            class="select cursor-pointer"
            disabled={isLoading['preferredLang']}
            onchange={(ev) => {
              const value = (ev.target as HTMLSelectElement).value
              update('preferredLang', value === '' ? null : value)
            }}
            data-test-id="preferred-lang-select"
          >
            <option value="" selected={user.preferredLang === null}>None</option>
            {#each PreferredLangs as lang}
              <option value={lang} selected={user.preferredLang === lang}>{lang}</option>
            {/each}
          </select>
          <span class="loading loading-spinner invisible !w-[14px] !h-[14px]" class:!visible={isLoading['preferredLang']}></span>
        </div>
        <div class="flex gap-4 items-center">
          <label class="flex gap-4 items-center cursor-pointer">
            <input
              type="checkbox"
              class="checkbox checkbox-xs"
              checked={user.shouldReceiveNewsletter}
              disabled={isLoading['shouldReceiveNewsletter']}
              onclick={(ev) => {
                const checked = (ev.target as HTMLInputElement).checked
                update('shouldReceiveNewsletter', checked)
              }}
              data-test-id="receive-newsletter-checkbox"
            />
            <span class="label">Receive Newsletter</span>
          </label>
          <span class="loading loading-spinner invisible !w-[14px] !h-[14px]" class:!visible={isLoading['shouldReceiveNewsletter']}></span>
        </div>
        <a href="/" class="link link-primary">Back to Home</a>
      </div>
    </div>
  </div>
</div>


<style lang="scss">
</style>
